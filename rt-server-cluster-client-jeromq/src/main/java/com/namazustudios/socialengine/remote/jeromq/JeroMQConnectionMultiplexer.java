package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.ConnectionMultiplexer;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.MultiException;
import com.namazustudios.socialengine.rt.jeromq.Identity;
import com.namazustudios.socialengine.rt.jeromq.Routing;
import com.namazustudios.socialengine.rt.remote.RoutingHeader;
import com.namazustudios.socialengine.rt.util.FinallyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Thread.interrupted;
import static java.util.stream.IntStream.*;
import static org.zeromq.ZMQ.DEALER;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMQ.ROUTER;
import static org.zeromq.ZMsg.recvMsg;

public class JeroMQConnectionMultiplexer implements ConnectionMultiplexer {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQConnectionMultiplexer.class);

    public static final String CONNECT_ADDR = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.connectAddress";

    public static final String DESTINATION_IDS = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionMultiplexer.destinationIds";

    private final AtomicReference<Thread> multiplexerThread = new AtomicReference<>();

    private Routing routing;

    private Identity identity;

    private ZContext zContext;

    private String connectAddress;

    private Set<String> destinationIds;

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

    public Set<String> getDestinationIds() {
        return destinationIds;
    }

    @Inject
    public void setDestinationIds(@Named(DESTINATION_IDS) Set<String> destinationIds) {
        this.destinationIds = destinationIds;
    }

    private class Multiplexer implements Runnable {

        @Override
        public void run() {

            FinallyAction action = FinallyAction.with(() -> {});

            try (final ZMQ.Poller poller = getzContext().createPoller(0);
                 final ZMQ.Socket backend = getzContext().createSocket(DEALER);
                 final Frontends frontends = new Frontends(poller)) {

                action = action.then(() -> getzContext().destroySocket(backend));

                backend.connect(getConnectAddress());

                final int backendIndex = poller.register(backend, POLLIN | POLLERR);

                getDestinationIds().forEach(frontends::bind);

                while (!interrupted()) {

                    if (poller.poll(1000) == 0) {
                        continue;
                    }

                    range(0, poller.getNext()).filter(index -> poller.getItem(index) != null).forEach(index -> {

                        final boolean input = poller.pollin(index);
                        final boolean error = poller.pollerr(index);

                        if (input && backendIndex == index) {
                            final ZMQ.Socket socket = poller.getSocket(index);
                            final ZMsg msg = recvMsg(socket);
                            logger.info("Replying {}", msg);
                            frontends.track(msg, index);
                            frontends.socketStream().forEach(frontend -> msg.duplicate().send(frontend));
                            msg.destroy();
                        } else if (input) {
                            final ZMQ.Socket socket = poller.getSocket(index);
                            final ZMsg msg = recvMsg(socket);
                            final UUID destination = frontends.getDestination(index);
                            insertRoutingHeader(msg, destination);
                            frontends.track(msg, index);
                            msg.send(backend);
                        } else if (error) {
                            throw new InternalException("Caught exception reading socket.");
                        }

                    });

                }

            } finally {
                action.perform();
            }

        }

        private void insertRoutingHeader(final ZMsg msg, final UUID destination) {

            final ZMsg identity = getIdentity().popIdentity(msg);
            logger.info("Muxing {} -> {}", identity, destination);

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

        private final Map<ZMsg, Integer> identities = new LinkedHashMap<>();

        private final ZMQ.Poller poller;

        public Frontends(ZMQ.Poller poller) {
            this.poller = poller;
        }

        public void track(final ZMsg msg, final int index) {

            final ZMsg identity = getIdentity().copyIdentity(msg);
            final Integer old = identities.put(identity, index);

            if (old != null && !old.equals(index)) {
                logger.warn("Recycled identity.");
            }

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

            if (!exceptionList.isEmpty()) {
                throw new MultiException(exceptionList);
            }

        }

        public UUID getDestination(final int index) {
            return frontends.get(index);
        }

        public int bind(final String destinationNodeId) {

            final ZMQ.Socket socket = getzContext().createSocket(ROUTER);
            final UUID destinationId = getRouting().getDestinationId(destinationNodeId);
            final String bindAddress = getRouting().getMultiplexedForDestinationId(destinationId);

            socket.bind(bindAddress);

            final int index = poller.register(socket, POLLIN | POLLERR);
            frontends.put(index, destinationId);

            return index;

        }

    }

}
