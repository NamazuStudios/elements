package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.ConnectionDemultiplexer;
import com.namazustudios.socialengine.rt.exception.MultiException;
import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.NodeNotFoundException;
import com.namazustudios.socialengine.rt.jeromq.Identity;
import com.namazustudios.socialengine.rt.remote.MalformedMessageException;
import com.namazustudios.socialengine.rt.jeromq.Routing;
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

import static com.namazustudios.socialengine.rt.jeromq.Identity.EMPTY_DELIMITER;
import static java.lang.Thread.interrupted;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMsg.recvMsg;

public class JeroMQConnectionDemultiplexer implements ConnectionDemultiplexer {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQConnectionDemultiplexer.class);

    public static final String BIND_ADDR = "com.namazustudios.socialengine.remote.jeromq.JeroMQConnectionDemultiplexer.bindAddress";

    private Routing routing;

    private Identity identity;

    private ZContext zContext;

    private String bindAddress;

    private Set<Node> nodeSet;

    private final AtomicReference<Thread> routerThread = new AtomicReference<>();

    @Override
    public void start() {

        final Thread thread = new Thread(new Demultiplexer());

        thread.setDaemon(true);
        thread.setName(JeroMQConnectionDemultiplexer.class.getSimpleName() + " thread");

        if (routerThread.compareAndSet(null, thread)) {
            thread.start();
        } else {
            throw new IllegalStateException("Demultiplexer already started.");
        }

    }

    @Override
    public void stop() {

        final Thread thread = routerThread.get();

        if (routerThread.compareAndSet(thread, null)) {

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

    public Set<Node> getNodeSet() {
        return nodeSet;
    }

    @Inject
    public void setNodeSet(Set<Node> nodeSet) {
        this.nodeSet = nodeSet;
    }

    private class Demultiplexer implements Runnable {

        @Override
        public void run() {

            FinallyAction action = FinallyAction.with(() -> {});

            try (final ZMQ.Poller poller = getzContext().createPoller(1);
                 final Backends backends = new Backends(poller);
                 final ZMQ.Socket frontend = getzContext().createSocket(ZMQ.ROUTER)) {

                action = FinallyAction.with(() -> getzContext().destroySocket(frontend));
                frontend.bind(getBindAddress());

                final int frontendIndex = poller.register(frontend, POLLIN | POLLERR);

                while (!interrupted()) {

                    final int index = poller.poll(1000);

                    if (index < 0) {
                        continue;
                    }

                    try {
                        routeMessages(backends, poller, index, frontend, frontendIndex);
                    } catch (MalformedMessageException ex) {
                        backends.close(index);
                    }

                }

            } catch (InterruptedException ex) {
                logger.info("Interrupted.  Exiting.");
            } catch (Exception ex) {
                logger.error("Caught exception closing router.", ex);
            } finally {
                action.perform();
            }

        }

        private void routeMessages(
                final Backends backends,
                final ZMQ.Poller poller, final int index,
                final ZMQ.Socket frontend, final int frontendIndex) {

            final boolean input = poller.pollin(index);
            final boolean error = poller.pollerr(index);

            if (input && index == frontendIndex) {

                final ZMsg msg = recvMsg(frontend);
                final RoutingHeader incomingRoutingHeader = stripRoutingHeader(msg);

                if (incomingRoutingHeader.status.get() == RoutingHeader.Status.CONTINUE) {
                    try {

                        final int route = backends.getBackend(incomingRoutingHeader.destination.get());
                        final ZMQ.Socket socket = poller.getSocket(route);
                        msg.send(socket);

                    } catch (NodeNotFoundException ex) {

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
                        response.send(frontend);

                    }
                }

            } else if (input) {
                final ZMQ.Socket socket = poller.getSocket(index);
                final ZMsg msg = recvMsg(socket);
                msg.send(frontend);
            } else if (error && index == frontendIndex) {
                throw new InternalException("Frontend socket encountered error: " + frontend.errno());
            } else if (error) {
                backends.close(index);
            } else {
                // This should never happen if the poller was setup properly, but if it is we should log the
                // situation and keep on moving.
                logger.warn("Condition neither error nor input.  Skipping IO Event.");
            }

        }

        private RoutingHeader stripRoutingHeader(final ZMsg msg) {

            final Iterator<ZFrame> zFrameIterator = msg.iterator();

            while (zFrameIterator.hasNext()) {
                final ZFrame frame = zFrameIterator.next();

                if (!Arrays.equals(frame.getData(), EMPTY_DELIMITER)) {
                    continue;
                }

                final ZFrame routeIdFrame;

                try {
                    routeIdFrame = zFrameIterator.next();
                } catch (NoSuchElementException ex) {
                    throw new MalformedMessageException("No frames after delimtier.", ex);
                }

                zFrameIterator.remove();

                final RoutingHeader routingHeader = new RoutingHeader();
                routingHeader.getByteBuffer().put(routeIdFrame.getData());

                return routingHeader;

            }

            throw new MalformedMessageException("No delimiter frame found.");

        }

    }

    private class Backends implements AutoCloseable {

        private final Map<UUID, Integer> backends = new HashMap<>();

        private final ZMQ.Poller poller;

        public Backends(final ZMQ.Poller poller) {
            this.poller = poller;
        }

        @Override
        public void close() throws Exception {
            final List<Exception> exceptionList = backends.values().stream().map(backend -> {

                final ZMQ.Socket socket = poller.getSocket(backend);

                if (socket == null) {
                    logger.warn("Missing socket at index {}", backend);
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

            }).filter(ex -> ex != null).collect(Collectors.toList());

            if (!exceptionList.isEmpty()) {
                throw new MultiException(exceptionList);
            }

        }

        public void close(final int index) {

            final Collection<Integer> indices = backends.values();

            while (indices.remove(index)) {

                final ZMQ.Socket socket = poller.getSocket(index);

                if (socket == null) {
                    continue;
                }

                poller.unregister(socket);

                try {
                    socket.close();
                } catch (Exception ex) {
                    logger.error("Unable to close socket.", ex);
                } finally {
                    getzContext().destroySocket(socket);
                }

            }

        }

        public int getBackend(final UUID destinationId) {
            return backends.computeIfAbsent(destinationId, did -> {

                final Node node = getNodeSet()
                    .stream()
                    .filter(n -> destinationId.equals(getRouting().getDestinationId(n.getId())))
                    .findFirst().orElseThrow(() -> new NodeNotFoundException());

                logger.info("Found route for node {} ({})", node.getId(), node.getName());

                final ZMQ.Socket socket = getzContext().createSocket(ZMQ.DEALER);
                final String routeAddress = getRouting().getAddressForDestinationId(destinationId);
                socket.connect(routeAddress);

                return poller.register(socket, POLLIN | POLLERR);

            });
        }

    }

}
