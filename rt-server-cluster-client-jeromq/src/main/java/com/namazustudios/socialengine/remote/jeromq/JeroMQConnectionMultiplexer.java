package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.ConnectionMultiplexer;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.MultiException;
import com.namazustudios.socialengine.rt.jeromq.Connection;
import com.namazustudios.socialengine.rt.jeromq.Routing;
import com.namazustudios.socialengine.rt.jeromq.RoutingTable;
import com.namazustudios.socialengine.rt.remote.RoutingHeader;
import com.namazustudios.socialengine.rt.util.FinallyAction;
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
import static com.namazustudios.socialengine.rt.remote.RoutingHeader.Status.CONTINUE;
import static java.lang.String.format;
import static java.lang.Thread.interrupted;
import static java.util.UUID.randomUUID;
import static java.util.stream.IntStream.range;
import static org.zeromq.ZMQ.DEALER;
import static org.zeromq.ZMQ.PULL;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMQ.ROUTER;
import static org.zeromq.ZMsg.recvMsg;
import static zmq.ZError.EHOSTUNREACH;

public class JeroMQConnectionMultiplexer implements ConnectionMultiplexer {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQConnectionMultiplexer.class);

    public static final String CONNECT_ADDR = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.connectAddress";

    public static final String DESTINATION_IDS = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.destinationIds";

    private final AtomicReference<Thread> multiplexerThread = new AtomicReference<>();

    private Routing routing;

    private ZContext zContext;

    private String connectAddress;

    private Set<String> destinationIds;

    private final String controlAddress = format("inproc://%s.control", randomUUID());

    @Override
    public void start() {

        final Thread thread = new Thread(new Multiplexer());

        thread.setDaemon(true);
        thread.setName(JeroMQConnectionMultiplexer.class.getSimpleName() + " thread");

        if (multiplexerThread.compareAndSet(null, thread)) {
            thread.start();
        } else {
            throw new IllegalStateException("Multiplexer already started.");
        }

    }

    @Override
    public void stop() {

        final Thread thread = multiplexerThread.get();

        if (multiplexerThread.compareAndSet(thread, null)) {

            thread.interrupt();

            try {
                thread.join();
            } catch (InterruptedException ex) {
                throw new InternalException("Interrupted while shutting down the connection router.", ex);
            }

        } else {
            throw new IllegalStateException("Multiplexer already started.");
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

    public String getConnectAddress() {
        return connectAddress;
    }

    @Inject
    public void setConnectAddress(@Named(CONNECT_ADDR) String connectAddress) {
        this.connectAddress = connectAddress;
    }

    public Set<String> getDestinationIds() {
        return destinationIds;
    }

    @Inject
    public void setDestinationIds(@Named(DESTINATION_IDS) Set<String> destinationIds) {
        this.destinationIds = destinationIds;
    }

    public String getControlAddress() {
        return controlAddress;
    }

    private class Multiplexer implements Runnable {

        @Override
        public void run() {

            try (final ZMQ.Poller poller = getzContext().createPoller(0);
                 final Connection backend = from(getzContext(), c -> c.createSocket(DEALER));
                 final Connection control = from(getzContext(), c -> c.createSocket(PULL));
                 final RoutingTable frontends = new RoutingTable(getzContext(), poller, this::bind)) {

                backend.socket().connect(getConnectAddress());

                final int backendIndex = poller.register(backend.socket(), POLLIN | POLLERR);
                getDestinationIds().stream().map(getRouting()::getDestinationId).forEach(frontends::open);

                while (!interrupted()) {

                    if ((poller.poll(1000)) == 0) {
                        continue;
                    }

                    range(0, poller.getNext()).filter(index -> poller.getItem(index) != null).forEach(index -> {
                        routeMessages(poller, backend.socket(), backendIndex, frontends, index);
                    });

                }

            } catch (Exception ex) {
                logger.error("Caught exception.  Exiting.", ex);
            }

        }

        private ZMQ.Socket bind(final UUID uuid) {
            final ZMQ.Socket socket = getzContext().createSocket(ROUTER);
            final String bindAddress = getRouting().getMultiplexedAddressForDestinationId(uuid);
            socket.setRouterMandatory(true);
            socket.bind(bindAddress);
            return socket;
        }

        private void routeMessages(final ZMQ.Poller poller,
                                   final ZMQ.Socket backend, final int backendIndex,
                                   final RoutingTable frontends, final int  index) {

            final boolean input = poller.pollin(index);
            final boolean error = poller.pollerr(index);

            if (input && backendIndex == index) {
                sendToFrontend(poller, index, frontends);
            } else if (input) {
                sendToBackend(poller, index, frontends, backend);
            } else if (error) {
                throw new InternalException("Caught exception reading socket.");
            }

        }

        private void sendToFrontend(final ZMQ.Poller poller, final int index, final RoutingTable frontends) {
            final ZMQ.Socket socket = poller.getSocket(index);
            final ZMsg msg = recvMsg(socket);

            final RoutingHeader routingHeader = getRouting().stripRoutingHeader(msg);

            if (routingHeader.status.get() == CONTINUE) {

                final UUID destination = routingHeader.destination.get();
                final ZMQ.Socket frontend = frontends.getSocket(destination);

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
                logger.error("Received {} route for destination {}", routingHeader.status.get(), routingHeader.destination.get());
            }

        }

        private void sendToBackend(final ZMQ.Poller poller, final int index,
                                   final RoutingTable frontends, final ZMQ.Socket backend) {

            final ZMQ.Socket socket = poller.getSocket(index);
            final ZMsg msg = recvMsg(socket);
            final UUID destination = frontends.getDestination(index);

            final RoutingHeader routingHeader = new RoutingHeader();
            routingHeader.status.set(CONTINUE);
            routingHeader.destination.set(destination);

            getRouting().insertRoutingHeader(msg, routingHeader);
            msg.send(backend);

        }

    }

}
