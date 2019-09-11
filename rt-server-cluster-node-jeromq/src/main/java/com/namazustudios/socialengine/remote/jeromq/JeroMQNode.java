package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.AsyncConnection;
import com.namazustudios.socialengine.rt.AsyncConnectionGroup;
import com.namazustudios.socialengine.rt.AsyncConnectionPool;
import com.namazustudios.socialengine.rt.AsyncConnectionService;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceBinding;
import com.namazustudios.socialengine.rt.remote.LocalInvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.Node;
import com.namazustudios.socialengine.rt.remote.NodeLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.namazustudios.socialengine.rt.AsyncConnection.Event.ERROR;
import static com.namazustudios.socialengine.rt.AsyncConnection.Event.READ;
import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.zeromq.SocketType.*;
import static org.zeromq.ZMQ.Socket;

public class JeroMQNode implements Node {

    private static final Logger staticLogger = LoggerFactory.getLogger(JeroMQNode.class);

    private static final String OUTBOUND_ADDR_FORMAT = "inproc://node/%s/out";

    public static final String MIN_CONNECTIONS = "com.namazustudios.socialengine.remote.jeromq.node.min.connections";

    public static final String MAX_CONNECTIONS = "com.namazustudios.socialengine.remote.jeromq.node.max.connections";

    private final AtomicReference<NodeContext> context = new AtomicReference<>();

    private String name;

    private NodeId nodeId;

    private int minConnections;

    private int maxConnections;

    private LocalInvocationDispatcher invocationDispatcher;

    private PayloadReader payloadReader;

    private PayloadWriter payloadWriter;

    private NodeLifecycle nodeLifecycle;

    private AsyncConnectionService<ZContext, ZMQ.Socket> asyncConnectionService;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    public String getOutboundAddr() {
        return format(OUTBOUND_ADDR_FORMAT, getNodeId().asString());
    }

    @Override
    public void start(final InstanceBinding binding) {

        final NodeContext c = new NodeContext();

        if (context.compareAndSet(null, c)) {
            c.logger.info("Starting up.");
            getNodeLifecycle().preStart();
            c.start(binding);
            c.logger.info("Started Node.");
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    private Logger loggerForNode() {
        final String prefix = JeroMQNode.class.getName();
        if (getName() != null) return LoggerFactory.getLogger(format("%s.%s", prefix, getName()));
        else if (getNodeId() != null) return LoggerFactory.getLogger(format("%s.%s", prefix, getNodeId().asString()));
        else return staticLogger;
    }

    @Override
    public void stop() {

        final NodeContext c = context.getAndSet(null);

        if (c == null) {
            throw new IllegalStateException("Already stopped.");
        } else {
            c.logger.info("Shutting down.");
            c.stop();
            c.logger.info("Shutdown.  Issuing NodeLifecycle stop command.");
            getNodeLifecycle().postStop();
            c.logger.info("Shutdown.  Issued NodeLifecycle stop command.");
        }

    }

    public LocalInvocationDispatcher getInvocationDispatcher() {
        return invocationDispatcher;
    }

    @Inject
    public void setInvocationDispatcher(LocalInvocationDispatcher invocationDispatcher) {
        this.invocationDispatcher = invocationDispatcher;
    }

    public PayloadReader getPayloadReader() {
        return payloadReader;
    }

    @Inject
    public void setPayloadReader(PayloadReader payloadReader) {
        this.payloadReader = payloadReader;
    }

    public PayloadWriter getPayloadWriter() {
        return payloadWriter;
    }

    @Inject
    public void setPayloadWriter(PayloadWriter payloadWriter) {
        this.payloadWriter = payloadWriter;
    }

    public AsyncConnectionService<ZContext, ZMQ.Socket> getAsyncConnectionService() {
        return asyncConnectionService;
    }

    @Inject
    public void setAsyncConnectionService(AsyncConnectionService<ZContext, ZMQ.Socket> asyncConnectionService) {
        this.asyncConnectionService = asyncConnectionService;
    }

    @Inject
    public void setNodeId(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    @Inject
    public void setName(@Named(NAME) String name) {
        this.name = name;
    }

    public int getMinConnections() {
        return minConnections;
    }

    @Inject
    public void setMinConnections(@Named(MIN_CONNECTIONS) int minConnections) {
        this.minConnections = minConnections;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    @Inject
    public void setMaxConnections(@Named(MAX_CONNECTIONS) int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public NodeLifecycle getNodeLifecycle() {
        return nodeLifecycle;
    }

    @Inject
    public void setNodeLifecycle(NodeLifecycle nodeLifecycle) {
        this.nodeLifecycle = nodeLifecycle;
    }

    private class NodeContext {

        private final Logger logger = loggerForNode();

        private AsyncConnection<ZContext, ZMQ.Socket> frontendConnection;

        private AsyncConnectionGroup mainConnectionGroup;

        private AsyncConnectionPool<ZContext, ZMQ.Socket> outboundConnectionPool;

        private final AtomicInteger dispatcherCount = new AtomicInteger();

        private final ExecutorService dispatchExecutorService = newCachedThreadPool(r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(format("%s %s.in #%d", getClass().getSimpleName(), getName(), dispatcherCount.incrementAndGet()));
            thread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));
            return thread;
        });

        public void start(final InstanceBinding instanceBinding) {

            final CountDownLatch latch = new CountDownLatch(3);

            outboundConnectionPool = getAsyncConnectionService().allocatePool(
                "JeroMQNode Outbound",
                getMinConnections(),
                getMaxConnections(),
                z -> {
                    final Socket socket = z.createSocket(PUSH);
                    socket.connect(getOutboundAddr());
                    return socket;
                });

            getAsyncConnectionService().group()
                .connection(z -> {
                    final Socket socket = z.createSocket(ROUTER);
                    socket.bind(instanceBinding.getBindAddress());
                    return socket;
                }, connection -> {
                    frontendConnection = connection;
                    connection.setEvents(READ, ERROR);
                    connection.onRead(this::onFrontendRead);
                    connection.onError(this::onFrontendError);
                    latch.countDown();
                })
                .connection(z -> {
                    final Socket socket = z.createSocket(PULL);
                    socket.bind(getOutboundAddr());
                    return socket;
                }, connection -> {
                    connection.setEvents(READ, ERROR);
                    connection.onRead(this::onBackendRead);
                    connection.onError(this::onBackendError);
                    latch.countDown();
                }).build(group -> {
                    mainConnectionGroup = group;
                    latch.countDown();
                });

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new InternalException(e);
            }

        }

        private void onFrontendRead(final AsyncConnection<ZContext, ZMQ.Socket> connection) {
            final ZMsg msg = ZMsg.recvMsg(connection.socket());
            dispatchExecutorService.submit(() -> dispatch(msg));
        }

        private void onFrontendError(final AsyncConnection<ZContext, ZMQ.Socket> connection) {
            logger.error("Frontend Connection Error {} - errno {}", connection, connection.socket().errno());
        }

        private void onBackendRead(final AsyncConnection<ZContext, ZMQ.Socket> connection) {
            final ZMsg msg = ZMsg.recvMsg(connection.socket());
            msg.send(frontendConnection.socket());
        }

        private void onBackendError(final AsyncConnection<ZContext, ZMQ.Socket> connection) {
            logger.error("Backend Connection Error {} - errno {}", connection, connection.socket().errno());
        }

        public void stop() {

            mainConnectionGroup.close();
            outboundConnectionPool.close();
            dispatchExecutorService.shutdownNow();

            try {
                if (!dispatchExecutorService.awaitTermination(10, MINUTES)) {
                    logger.error("Terminating dispatchers timed out.");
                }
            } catch (InterruptedException e) {
                logger.error("Interrupted while shutting down Node Thread Pool", e);
            }

        }

        private void dispatch(final ZMsg msg) {

            final JeroMQNodeInvocation invocation = new JeroMQNodeInvocation(
                msg,
                getInvocationDispatcher(),
                getPayloadReader(),
                getPayloadWriter(),
                outboundConnectionPool
            );

            invocation.dispatch();

        }

    }

}


