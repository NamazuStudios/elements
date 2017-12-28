package com.namazustudios.socialengine.appnode.jeromq;

import com.namazustudios.socialengine.appnode.ConnectionRouter;
import com.namazustudios.socialengine.exception.MultiException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.remote.PackedUUID;
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

public class JeroMQConnectionRouter implements ConnectionRouter {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQConnectionRouter.class);

    public static final String BIND_ADDR = "com.namazustudios.socialengine.appnode.jeromq.JeroMQConnectionRouter.bindAddress";

    private Routing routing;

    private ZContext zContext;

    private String bindAddress;

    private final AtomicReference<Thread> routerThread = new AtomicReference<>();

    @Override
    public void start() {

        final Thread thread = new Thread(new Router());

        if (routerThread.compareAndSet(null, thread)) {
            thread.start();
        } else {
            throw new IllegalStateException("Router already started.");
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
            throw new IllegalStateException("Router already started.");
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

    public String getBindAddress() {
        return bindAddress;
    }

    @Inject
    public void setBindAddress(@Named(BIND_ADDR) String bindAddress) {
        this.bindAddress = bindAddress;
    }

    private class Router implements Runnable {

        @Override
        public void run() {

            FinallyAction action = FinallyAction.with(() -> {});

            try (final ZMQ.Poller poller = getzContext().createPoller(1);
                 final Routes routes = new Routes(poller);
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
                        routeMessages(routes, poller, index, frontend, frontendIndex);
                    } catch (MalformedMessageException ex) {
                        routes.close(index);
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
                final Routes routes,
                final ZMQ.Poller poller, final int index,
                final ZMQ.Socket frontend, final int frontendIndex) {

            if (poller.pollin(index)) if (index == frontendIndex) {
                final ZMsg msg = recvMsg(frontend);
                final UUID routeId = stripRouteId(msg);
                final int route = routes.getRoute(routeId);
                final ZMQ.Socket socket = poller.getSocket(route);
                msg.send(socket);
            } else {
                final ZMQ.Socket socket = poller.getSocket(index);
                final ZMsg msg = recvMsg(socket);
                msg.send(frontend);
            } else if (poller.pollerr(index)) if (index == frontendIndex) {
                throw new InternalException("Frontend socket encountered error: " + frontend.errno());
            } else {
                routes.close(index);
            }

        }

        private UUID stripRouteId(final ZMsg msg) {

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

                final PackedUUID routeId = new PackedUUID();
                routeId.getByteBuffer().put(routeIdFrame.getData());

                return routeId.get();

            }

            throw new MalformedMessageException("No delimiter frame found.");

        }

    }

    private class Routes implements AutoCloseable {

        private final Map<UUID, Integer> routes = new HashMap<>();

        private final ZMQ.Poller poller;

        public Routes(final ZMQ.Poller poller) {
            this.poller = poller;
        }

        @Override
        public void close() throws Exception {

            final List<Exception> exceptionList = routes.values().stream().map(route -> {

                final ZMQ.Socket socket = poller.getSocket(route);

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
            final Collection<Integer> indices = routes.values();
            while (indices.remove(index)) {

                final ZMQ.Socket socket = poller.getSocket(index);

                if (socket == null) {
                    continue;
                }

                try {
                    poller.unregister(socket);
                } catch (Exception ex) {
                    logger.error("Unable to unregister socket {} ", socket);
                }

                try {
                    socket.close();
                } catch (Exception ex) {
                    logger.error("Unable to close socket.", ex);
                } finally {
                    getzContext().destroySocket(socket);
                }

            }
        }

        public int getRoute(final UUID routeId) {
            return routes.computeIfAbsent(routeId, rid -> {
                final ZMQ.Socket socket = getzContext().createSocket(ZMQ.DEALER);
                final String routeAddress = getRouting().getAddressForRouteId(routeId);
                socket.connect(routeAddress);
                return poller.register(socket, POLLIN | POLLERR);
            });
        }

    }

}
