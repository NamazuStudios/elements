package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.ConnectionDemultiplexer;
import com.namazustudios.socialengine.rt.exception.MultiException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.NodeNotFoundException;
import com.namazustudios.socialengine.rt.jeromq.Connection;
import com.namazustudios.socialengine.rt.jeromq.Identity;
import com.namazustudios.socialengine.rt.jeromq.RoutingTable;
import com.namazustudios.socialengine.rt.remote.MalformedMessageException;
import com.namazustudios.socialengine.rt.jeromq.Routing;
import com.namazustudios.socialengine.rt.remote.RoutingHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;
import org.zeromq.ZMsg;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.namazustudios.socialengine.rt.jeromq.Connection.from;
import static com.namazustudios.socialengine.rt.jeromq.Identity.EMPTY_DELIMITER;
import static com.namazustudios.socialengine.rt.remote.RoutingHeader.Status.CONTINUE;
import static java.lang.Thread.interrupted;
import static java.util.stream.IntStream.range;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMQ.ROUTER;
import static org.zeromq.ZMsg.recvMsg;
import static zmq.ZError.EHOSTUNREACH;

public class JeroMQConnectionDemultiplexer implements ConnectionDemultiplexer {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQConnectionDemultiplexer.class);

    public static final String BIND_ADDR = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionDemultiplexer.bindAddress";

    public static final String DESTINATION_IDS = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionDemultiplexer.destinationIds";

    private Routing routing;

    private Identity identity;

    private ZContext zContext;

    private String bindAddress;

    private Set<String> destinationIds;

    private final AtomicReference<Thread> routerThread = new AtomicReference<>();

    @Override
    public void start() {

        final Thread thread = new Thread(new Demultiplexer());

        thread.setDaemon(true);
        thread.setName(JeroMQConnectionDemultiplexer.class.getSimpleName() + " thread");

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

    public Set<String> getDestinationIds() {
        return destinationIds;
    }

    @Inject
    public void setDestinationIds(@Named(DESTINATION_IDS) Set<String> destinationIds) {
        this.destinationIds = destinationIds;
    }

    private class Demultiplexer implements Runnable {

        @Override
        public void run() {

            try (final ZMQ.Poller poller = getzContext().createPoller(1);
                 final Connection frontend = from(getzContext(), c -> c.createSocket(ROUTER));
                 final RoutingTable backends = new RoutingTable(getzContext(), poller, this::connect)) {

                frontend.socket().setRouterMandatory(true);
                frontend.socket().bind(getBindAddress());

                final int frontendIndex = poller.register(frontend.socket(), POLLIN | POLLERR);
                logger.info("Started.");

                while (!interrupted()) {

                    if (poller.poll(1000) == 0) {
                        continue;
                    }

                    range(0, poller.getNext()).filter(index -> poller.getItem(index) != null).forEach(index -> {
                        try {
                            routeMessages(backends, poller, index, frontend.socket(), frontendIndex);
                        } catch (MalformedMessageException ex) {
                            logger.warn("Got malformed message.  Closing connection to peer.", ex);
                            backends.close(index);
                        }
                    });

                }

            }  catch (Exception ex) {
                logger.error("Caught exception closing router.", ex);
            }

        }

        private ZMQ.Socket connect(final UUID destinationId) {

            final String nodeId = getDestinationIds()
                    .stream()
                    .filter(nid -> destinationId.equals(getRouting().getDestinationId(nid)))
                    .findFirst().orElseThrow(() -> new NodeNotFoundException());

            final ZMQ.Socket socket = getzContext().createSocket(ZMQ.DEALER);
            final String routeAddress = getRouting().getDemultiplexedAddressForDestinationId(destinationId);
            logger.info("Connecting to {} through {}", nodeId, routeAddress);

            socket.connect(routeAddress);
            return socket;

        }

        private void routeMessages(
                final RoutingTable backends,
                final ZMQ.Poller poller, final int index,
                final ZMQ.Socket frontend, final int frontendIndex) {

            final boolean input = poller.pollin(index);
            final boolean error = poller.pollerr(index);

            if (input && index == frontendIndex) {
                sendToBackend(frontend, backends);
            } else if (input) {
                sendToFrontend(index, frontend, backends);
            } else if (error && index == frontendIndex) {
                throw new InternalException("Frontend socket encountered error: " + frontend.errno());
            } else if (error) {
                backends.close(index);
            }

        }

        private void sendToBackend(final ZMQ.Socket frontend, final RoutingTable backends) {

            final ZMsg msg = recvMsg(frontend);
            final RoutingHeader incomingRoutingHeader = getRouting().stripRoutingHeader(msg);

            if (incomingRoutingHeader.status.get() == CONTINUE) {
                try {
                    sendOrDrop(incomingRoutingHeader, backends, msg);
                } catch (NodeNotFoundException ex) {
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

            // Without this check here, we could have an errant client constantly creating sockets in
            // this instance needlessly consuming resources/memory that will never get used.  Therefore, we
            // must limit or prevent this from happening.

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

    }

}
