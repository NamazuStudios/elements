package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.jeromq.*;
import com.namazustudios.socialengine.rt.remote.RoutingHeader;
import com.namazustudios.socialengine.rt.util.SyncWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import javax.inject.Inject;
import java.util.*;

import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType.STATUS_RESPONSE;
import static com.namazustudios.socialengine.rt.jeromq.Connection.from;
import static com.namazustudios.socialengine.rt.jeromq.JeroMQSocketHost.send;
import static com.namazustudios.socialengine.rt.remote.RoutingHeader.Status.CONTINUE;
import static java.lang.Thread.interrupted;
import static java.util.stream.IntStream.range;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.*;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMsg.recvMsg;
import static zmq.ZError.EHOSTUNREACH;

/**
 * Threaded manager for a multiplexed ZMQ connection.
 */
public class JeroMQMultiplexedConnection implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQMultiplexedConnection.class);

    private final SyncWait<Void> connectSyncWait = new SyncWait<Void>(logger);

    private final String controlAddress;

    private volatile Routing routing;

    private volatile ZContext zContext;

    public JeroMQMultiplexedConnection(final String controlAddress,
                                       final ZContext zContext,
                                       final Routing routing) {
        this.controlAddress = controlAddress;
        this.zContext = zContext;
        this.routing = routing;
    }

    public void waitForConnect() {
        connectSyncWait.get();
    }

    @Override
    public void run() {

        try (final ZContext context = shadow(zContext);
             final ZMQ.Poller poller = context.createPoller(0);
             final BackendChannelTable backendChannelTable = new BackendChannelTable(
                     context,
                     poller,
                     backendAddress -> bind(context, backendAddress),
                     inprocIdentifier -> bind(context, inprocIdentifier)
             );
             final Connection control = from(context, c -> c.createSocket(PULL));
             ) {

            final int controlIndex;

            try {
                control.socket().bind(controlAddress);
                controlIndex = poller.register(control.socket(), POLLIN | POLLERR);
                connectSyncWait.getResultConsumer().accept(null);
            } catch (Exception ex) {
                connectSyncWait.getErrorConsumer().accept(ex);
                return;
            }

            while (!interrupted()) {

                if (poller.poll(5000) < 0) {
                    logger.info("Interrupted.  Exiting gracefully.");
                    break;
                }

                range(0, poller.getNext()).filter(index -> poller.getItem(index) != null).forEach(index -> {

                    final boolean input = poller.pollin(index);
                    final boolean error = poller.pollerr(index);

                    if (input) {
                        final ZMQ.Socket socket = poller.getSocket(index);
                        final ZMsg msg = recvMsg(socket);



                        if (index == backendIndex) {
                            sendToInprocChannel(poller, index, frontends);
                        } else if (index == controlIndex) {
                            handleControlMessage(control.socket(), frontends);
                        } else {
                            sendToBackend(poller, index, frontends, backend.socket());
                        }
                    } else if (error) {
                        throw new InternalException("Poller error on socket: " + poller.getSocket(index));
                    }

                });

            }

        }

    }

    private ZMQ.Socket bind(final ZContext context, final String backendAddress) {
        final ZMQ.Socket backendSocket = context.createSocket(DEALER);
        backendSocket.connect(backendAddress);
        return backendSocket;
    }

    private ZMQ.Socket bind(final ZContext context, final UUID inprocIdentifier) {
        final ZMQ.Socket inprocSocket = context.createSocket(ROUTER);
        final String bindAddress = getRouting().getMultiplexedAddressForDestinationId(inprocIdentifier);
        inprocSocket.setRouterMandatory(true);
        inprocSocket.bind(bindAddress);
        return inprocSocket;
    }

    private void sendToInprocChannel(final ZMsg msg, final BackendChannelTable backendChannelTable) {
        final RoutingHeader routingHeader = getRouting().stripRoutingHeader(msg);

        if (routingHeader.status.get() == CONTINUE) {

            final UUID inprocIdentifier = routingHeader.inprocIdentifier.get();
            final ZMQ.Socket frontend = frontends.getSocket(inprocIdentifier);

            try {
                msg.send(frontend);
            } catch (ZMQException ex) {
                if (ex.getErrorCode() == EHOSTUNREACH) {
                    logger.warn("Host unreachable.  Dropping message.");
                } else {
                    throw ex;
                }
            }

        } else {
            logger.error("Received {} route for inprocIdentifier {}", routingHeader.status.get(), routingHeader.inprocIdentifier.get());
        }

    }

    private void sendToBackend(final ZMQ.Poller poller, final int index,
                               final InprocChannelTable frontends, final ZMQ.Socket backend) {

        final ZMQ.Socket socket = poller.getSocket(index);
        final ZMsg msg = recvMsg(socket);
        final UUID inprocIdentifier = frontends.getInprocIdentifier(index);

        final RoutingHeader routingHeader = new RoutingHeader();
        routingHeader.status.set(CONTINUE);
        routingHeader.inprocIdentifier.set(inprocIdentifier);

        getRouting().insertRoutingHeader(msg, routingHeader);
        msg.send(backend);

    }

    private void handleControlMessage(final ZMQ.Socket control, final InprocChannelTable frontends) {

        final ZMsg msg = ZMsg.recvMsg(control);
        final CommandPreamble preamble = new CommandPreamble();

        preamble.getByteBuffer().put(msg.pop().getData());

        switch(preamble.commandType.get()) {
            case STATUS_REQUEST:
                send(control, STATUS_RESPONSE, new StatusResponse().getByteBuffer());
                break;
            case ROUTING_COMMAND:
                final RoutingCommand command = new RoutingCommand();
                command.getByteBuffer().put(msg.pop().getData());
                frontends.process(command);
                break;
            default:
                logger.error("Unexpected command: {}", preamble.commandType.get());
        }

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
}