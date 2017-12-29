package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.ConnectionMultiplexer;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.MultiException;
import com.namazustudios.socialengine.rt.jeromq.Identity;
import com.namazustudios.socialengine.rt.jeromq.Routing;
import com.namazustudios.socialengine.rt.remote.MalformedMessageException;
import com.namazustudios.socialengine.rt.remote.RoutingHeader;
import com.namazustudios.socialengine.rt.util.FinallyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.jeromq.Identity.EMPTY_DELIMITER;
import static java.lang.Thread.interrupted;
import static org.zeromq.ZMQ.DEALER;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMQ.ROUTER;
import static org.zeromq.ZMQ.poll;
import static org.zeromq.ZMsg.recvMsg;

public class JeroMQConnectionMultiplexer implements ConnectionMultiplexer {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQConnectionMultiplexer.class);

    public static final String CONNECT_ADDR = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.connectAddress";

    public static final String DESTINATION_IDS = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.destinationNodeIds";

    private final AtomicReference<Thread> multiplexerThread = new AtomicReference<>();

    private Routing routing;

    private Identity identity;

    private ZContext zContext;

    private String connectAddress;

    private Set<String> destinationNodeIds;

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

    public Identity getIdentity() {
        return identity;
    }

    @Inject
    public void setIdentity(Identity identity) {
        this.identity = identity;
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

    public Set<String> getDestinationNodeIds() {
        return destinationNodeIds;
    }

    @Inject
    public void setDestinationNodeIds(@Named(DESTINATION_IDS) Set<String> destinationNodeIds) {
        this.destinationNodeIds = destinationNodeIds;
    }

    private class Multiplexer implements Runnable {

        @Override
        public void run() {

            FinallyAction action = FinallyAction.with(() -> {});

            try (final ZMQ.Poller poller = getzContext().createPoller(0);
                 final ZMQ.Socket backend = getzContext().createSocket(DEALER);
                 final Frontends frontends = new Frontends(poller)) {

                backend.connect(getConnectAddress());

                action = action.then(() -> getzContext().destroySocket(backend));
                final int backendIndex = poller.register(backend, POLLIN | POLLERR);

                getDestinationNodeIds().forEach(frontends::bind);

                while (!interrupted()) {

                    final int index = poller.poll(1000);

                    if (index < 0) {
                        continue;
                    }

                    final boolean input = poller.pollin(index);
                    final boolean error = poller.pollerr(index);

                    if (input && backendIndex == index) {
                        final ZMQ.Socket socket = poller.getSocket(index);
                        final ZMsg msg = recvMsg(socket);
                        frontends.socketStream().forEach(frontend -> msg.send(socket, false));
                    } else if (input) {
                        final ZMQ.Socket socket = poller.getSocket(index);
                        final ZMsg msg = recvMsg(socket);
                        final UUID destination = frontends.getDestination(index);
                        insertRoutingHeader(msg, destination);
                        msg.send(backend);
                    } else if (error) {
                        throw new InternalException("Caught exception reading socket.");
                    } else {
                        logger.warn("Unexpected polling event. Ignoring.");
                    }

                }

            } finally {
                action.perform();
            }

        }

        private void insertRoutingHeader(final ZMsg msg, final UUID destination) {

            final ZMsg identity = getIdentity().popIdentity(msg);

            final RoutingHeader routingHeader = new RoutingHeader();
            routingHeader.status.set(RoutingHeader.Status.CONTINUE);
            routingHeader.destination.set(destination);

            final byte[] routingHeaderBytes = new byte[routingHeader.size()];
            routingHeader.getByteBuffer().get(routingHeaderBytes);

            msg.push(routingHeaderBytes);
            getIdentity().pushIdentity(msg, identity);

        }

    }

    private class Frontends implements AutoCloseable {

        private final Map<Integer, UUID> frontends = new LinkedHashMap<>();

        private final ZMQ.Poller poller;

        public Frontends(ZMQ.Poller poller) {
            this.poller = poller;
        }

        public Stream<ZMQ.Socket> socketStream() {
            return frontends.keySet().stream()
                .map(index -> poller.getSocket(index))
                .filter(socket -> socket != null);
        }

        @Override
        public void close() {
            final List<Exception> exceptionList = frontends.keySet().stream().map(frontend -> {

                final ZMQ.Socket socket = poller.getSocket(frontend);

                if (socket == null) {
                    logger.warn("No frontend socket at index {}", frontend);
                    return null;
                }

                poller.unregister(socket);

                try {
                    socket.close();
                    return null;
                } catch (Exception ex) {
                    return ex;
                } finally {
                    getzContext().destroySocket(socket);
                }

            }).filter(e -> e != null).collect(Collectors.toList());

            if (exceptionList.isEmpty()) {
                throw new MultiException(exceptionList);
            }

        }

        public UUID getDestination(final int index) {
            return frontends.get(index);
        }

        public int bind(final String destinationNodeId) {

            final ZMQ.Socket socket = getzContext().createSocket(ROUTER);
            final UUID destinationId = getRouting().getDestinationId(destinationNodeId);
            final String bindAddress = getRouting().getAddressForDestinationId(destinationId);

            socket.bind(bindAddress);

            final int index = poller.register(socket, POLLIN | POLLERR);
            frontends.put(index, destinationId);

            return index;

        }

    }

}
