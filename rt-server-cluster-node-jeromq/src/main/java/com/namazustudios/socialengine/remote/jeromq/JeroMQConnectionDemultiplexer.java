package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.ConnectionDemultiplexer;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.jeromq.*;
import com.namazustudios.socialengine.rt.remote.MalformedMessageException;
import com.namazustudios.socialengine.rt.remote.RoutingHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType.ROUTING_COMMAND;
import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType.ROUTING_COMMAND_ACK;
import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType.STATUS_RESPONSE;
import static com.namazustudios.socialengine.rt.jeromq.Connection.from;
import static com.namazustudios.socialengine.rt.jeromq.Identity.EMPTY_DELIMITER;
import static com.namazustudios.socialengine.rt.jeromq.JeroMQSocketHost.send;
import static com.namazustudios.socialengine.rt.jeromq.RoutingCommand.Action.CLOSE;
import static com.namazustudios.socialengine.rt.jeromq.RoutingCommand.Action.OPEN;
import static com.namazustudios.socialengine.rt.remote.RoutingHeader.Status.CONTINUE;
import static java.lang.String.format;
import static java.lang.Thread.interrupted;
import static java.util.UUID.randomUUID;
import static java.util.stream.IntStream.range;
import static org.zeromq.ZMQ.*;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMsg.recvMsg;
import static zmq.ZError.EHOSTUNREACH;

public class JeroMQConnectionDemultiplexer implements ConnectionDemultiplexer {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQConnectionDemultiplexer.class);

    public static final String BIND_ADDR = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionDemultiplexer.bindAddress";

    public static final String CONTROL_BIND_ADDR = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionDemultiplexer.controlBindAddress";

    private Routing routing;

    private Identity identity;

    private ZContext zContext;

    private String bindAddress;

    private String controlBindAddress;

    private final AtomicReference<Thread> routerThread = new AtomicReference<>();

    private final String controlAddress = format("inproc://%s.control", randomUUID());

    @Override
    public void start() {

        final Thread thread = new Thread(new Demultiplexer());

        thread.setDaemon(true);
        thread.setName(JeroMQConnectionDemultiplexer.class.getSimpleName());
        thread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));

        if (routerThread.compareAndSet(null, thread)) {
            logger.info("Starting up.");
            thread.start();
        } else {
            throw new IllegalStateException("Demultiplexer already started.");
        }

    }

    @Override
    public void stop() {

        final Thread thread = routerThread.get();

        if (routerThread.compareAndSet(thread, null)) {

            logger.info("Shutting down.");
            thread.interrupt();

            try {
                thread.join();
            } catch (InterruptedException ex) {
                throw new InternalException("Interrupted while shutting down the connection router.", ex);
            }

        } else {
            throw new IllegalStateException("Demultiplexer already started.");
        }

    }

    @Override
    public String getBindAddress(UUID uuid) {
        return getRouting().getDemultiplexedAddressForDestinationId(uuid);
    }

    @Override
    public UUID getDestinationUUIDForNodeId(String destinationNodeId) {
        return getRouting().getDestinationId(destinationNodeId);
    }

    @Override
    public void open(UUID destination) {
        final RoutingCommand command = new RoutingCommand();
        command.action.set(OPEN);
        command.destination.set(destination);
        issue(command);
    }

    @Override
    public void close(UUID destination) {
        final RoutingCommand command = new RoutingCommand();
        command.action.set(CLOSE);
        command.destination.set(destination);
        issue(command);
    }

    private void issue(final RoutingCommand command) {
        try (final Connection connection = from(ZContext.shadow(getzContext()), c -> c.createSocket(REQ))) {
            connection.socket().connect(getControlAddress());
            send(connection.socket(), ROUTING_COMMAND, command.getByteBuffer());
            connection.socket().recv();
        }
    }

    public Identity getIdentity() {
        return identity;
    }

    @Inject
    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public Routing getRouting() {
        return routing;
    }

    @Inject
    public void setRouting(Routing routing) {
        this.routing = routing;
    }

    public ZContext getzContext() {
        return zContext;
    }

    @Inject
    public void setzContext(ZContext zContext) {
        this.zContext = zContext;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    @Inject
    public void setBindAddress(@Named(BIND_ADDR) String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public String getControlBindAddress() {
        return controlBindAddress;
    }

    @Inject
    public void setControlBindAddress(@Named(CONTROL_BIND_ADDR) String controlBindAddress) {
        this.controlBindAddress = controlBindAddress;
    }

    public String getControlAddress() {
        return controlAddress;
    }

    private class Demultiplexer implements Runnable {

        @Override
        public void run() {

            try (final ZContext context = ZContext.shadow(getzContext());
                 final ZMQ.Poller poller = context.createPoller(1);
                 final Connection frontend = from(getzContext(), c -> c.createSocket(ROUTER));
                 final Connection control = from(getzContext(), c -> c.createSocket(REP));
                 final RoutingTable backends = new RoutingTable(getzContext(), poller, this::connect);
                 final MonitorThread monitorThread = new MonitorThread(getClass().getSimpleName(), logger, context, frontend.socket())) {

                monitorThread.start();
                frontend.socket().setRouterMandatory(true);
                frontend.socket().bind(getBindAddress());
                control.socket().bind(getControlAddress());
                control.socket().bind(getControlBindAddress());

                final int frontendIndex = poller.register(frontend.socket(), POLLIN | POLLERR);
                final int controlIndex = poller.register(control.socket(),  POLLIN | POLLERR);

                logger.info("Started.");

                while (!interrupted()) {

                    if (poller.poll(5000) < 0) {
                        logger.info("Interrupted.  Exiting gracefully.");
                        break;
                    }

                    range(0, poller.getNext()).filter(index -> poller.getItem(index) != null).forEach(index -> {

                        final boolean input = poller.pollin(index);
                        final boolean error = poller.pollerr(index);

                        try {

                            if (input) {
                                if (index == frontendIndex) {
                                    sendToBackend(frontend.socket(), backends);
                                } else if (index == controlIndex) {
                                    handleControlMessage(control.socket(), backends);
                                } else {
                                    sendToFrontend(index, frontend.socket(), backends);
                                }
                            } else if (error) {
                                if (frontendIndex == index) {
                                    throw new InternalException("Frontend socket encountered error: " + frontend.socket().errno());
                                } else {
                                    backends.close(index);
                                }
                            }

                        } catch (MalformedMessageException ex) {
                            logger.warn("Got malformed message.  Closing connection to peer.", ex);
                            backends.close(index);
                        }

                    });

                }

            }  catch (Exception ex) {
                logger.error("Exiting with error.", ex);
            }

        }

        private ZMQ.Socket connect(final UUID destinationId) {
            final ZMQ.Socket socket = getzContext().createSocket(ZMQ.DEALER);
            final String routeAddress = getRouting().getDemultiplexedAddressForDestinationId(destinationId);
            logger.info("Connecting to {} through {}", destinationId, routeAddress);
            socket.connect(routeAddress);
            return socket;
        }

        private void sendToBackend(final ZMQ.Socket frontend, final RoutingTable backends) {

            final ZMsg msg = recvMsg(frontend);
            final RoutingHeader incomingRoutingHeader = getRouting().stripRoutingHeader(msg);

            if (incomingRoutingHeader.status.get() == CONTINUE) {
                if (backends.hasDestination(incomingRoutingHeader.destination.get())) {
                    sendOrDrop(incomingRoutingHeader, backends, msg);
                } else {
                    sendDeadRoute(incomingRoutingHeader, backends, msg);
                }
            } else {
                logger.warn("Got status {} from message.  Dropping.", incomingRoutingHeader.status.get());
            }

        }

        private void sendToFrontend(final int index, final ZMQ.Socket frontend, final RoutingTable backends) {

            final ZMQ.Socket socket = backends.getPoller().getSocket(index);
            final ZMsg msg = recvMsg(socket);
            final UUID destination = backends.getDestination(index);

            final RoutingHeader routingHeader = new RoutingHeader();
            routingHeader.status.set(CONTINUE);
            routingHeader.destination.set(destination);
            getRouting().insertRoutingHeader(msg, routingHeader);

            msg.send(frontend);

        }

        private void sendDeadRoute(final RoutingHeader incomingRoutingHeader, final RoutingTable backends, final ZMsg msg) {

            final ZMQ.Socket socket = backends.getSocket(incomingRoutingHeader.destination.get());

            if (socket == null) {
                return;
            }

            final RoutingHeader outgoingRoutingHeader = new RoutingHeader();
            outgoingRoutingHeader.status.set(RoutingHeader.Status.DEAD);
            outgoingRoutingHeader.destination.set(incomingRoutingHeader.destination.get());

            final byte[] outgoingRoutingHeaderBytes = new byte[outgoingRoutingHeader.size()];
            outgoingRoutingHeader.getByteBuffer().get(outgoingRoutingHeaderBytes);

            final ZMsg response = getIdentity().popIdentity(msg);
            response.add(EMPTY_DELIMITER);
            response.add(outgoingRoutingHeaderBytes);
            sendOrDrop(socket, msg);

        }

        private void sendOrDrop(final RoutingHeader incomingRoutingHeader,
                                final RoutingTable backends,
                                final ZMsg msg) {
            final int index = backends.open(incomingRoutingHeader.destination.get());
            final ZMQ.Socket socket = backends.getPoller().getSocket(index);
            sendOrDrop(socket, msg);
        }

        private void sendOrDrop(final ZMQ.Socket socket, final ZMsg msg) {
            try {
                msg.send(socket);
            } catch (ZMQException ex) {
                if (ex.getErrorCode() == EHOSTUNREACH) {
                    logger.warn("Frontend host unreachable.  Dropping message.");
                } else {
                    throw ex;
                }
            }
        }

        private void handleControlMessage(final ZMQ.Socket control, final RoutingTable backends) {
            final ZMsg msg = ZMsg.recvMsg(control);
            final CommandPreamble preamble = new CommandPreamble();

            preamble.getByteBuffer().put(msg.pop().getData());

            switch(preamble.commandType.get()) {
                case STATUS_REQUEST:
                    send(control, STATUS_RESPONSE, new StatusResponse().getByteBuffer());
                    break;
                case ROUTING_COMMAND:
                    send(control, ROUTING_COMMAND_ACK, new RoutingCommandAcknowledgement().getByteBuffer());
                    final RoutingCommand command = new RoutingCommand();
                    command.getByteBuffer().put(msg.pop().getData());
                    backends.process(command);
                    break;
                default:
                    logger.error("Unexpected command: {}", preamble.commandType.get());
            }

        }

    }

}
