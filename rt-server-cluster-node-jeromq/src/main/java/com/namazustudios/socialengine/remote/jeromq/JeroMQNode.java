package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.jeromq.IdentityUtil;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.remote.RequestHeader;
import com.namazustudios.socialengine.rt.remote.ResponseHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMsg;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.jeromq.IdentityUtil.EMPTY_DELIMITER;
import static com.namazustudios.socialengine.rt.remote.MessageType.INVOCATION_ERROR;
import static java.lang.String.format;
import static java.lang.Thread.interrupted;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.nameUUIDFromBytes;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.zeromq.ZMQ.*;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;

public class JeroMQNode implements Node {

    private static final Logger staticLogger = LoggerFactory.getLogger(JeroMQNode.class);

    private static final String INBOUND_ADDR_FORMAT = "inproc://node.%s.in";

    private static final String OUTBOUND_ADDR_FORMAT = "inproc://node.%s.out";

    public static final String ID = "com.namazustudios.socialengine.remote.jeromq.JeroMQNode.id";

    public static final String NAME = "com.namazustudios.socialengine.remote.jeromq.JeroMQNode.name";

    public static final String BIND_ADDRESS = "com.namazustudios.socialengine.remote.jeromq.JeroMQNode.bindAddress";

    private final AtomicReference<NodeContext> nodeContext = new AtomicReference<>();

    private UUID instanceUuid;

    private NodeId nodeId;

    private String name;

    private ZContext zContext;

    private String  bindAddress;

    private InvocationDispatcher invocationDispatcher;

    private PayloadReader payloadReader;

    private PayloadWriter payloadWriter;

    private Provider<ConnectionPool> connectionPoolProvider;

    private NodeLifecycle nodeLifecycle;

    private Logger logger = staticLogger;

    @Override
    public String getName() {
        return name;
    }

    public String getOutboundAddr() {
        return format(OUTBOUND_ADDR_FORMAT, getNodeId().getApplicationUuid().toString());
    }

    private void buildNodeIdIfPossible() {
        // name may be null, which signifies that the node is the master node
        if (getInstanceUuid() != null) {
            final UUID instanceUuid = getInstanceUuid();
            final UUID nodeUuid;
            if (getName() != null) {
                nodeUuid = nameUUIDFromBytes(getName().getBytes(UTF_8));
            }
            else {
                nodeUuid = null;
            }

            nodeId = new NodeId(instanceUuid, nodeUuid);
        }
    }

    @Override
    public void start() {

        final NodeContext c = new NodeContext();

        if (nodeContext.compareAndSet(null, c)) {
            logger.info("Starting up.");
            getNodeLifecycle().start();
            c.start();
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    @Override
    public void stop() {

        final NodeContext c = nodeContext.get();

        if (nodeContext.compareAndSet(c, null)) {
            logger.info("Shutting down.");
            c.stop();
            getNodeLifecycle().shutdown();
        } else {
            throw new IllegalStateException("Already stopped.");
        }

    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
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
    public void setBindAddress(@Named(BIND_ADDRESS) String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public InvocationDispatcher getInvocationDispatcher() {
        return invocationDispatcher;
    }

    @Inject
    public void setInvocationDispatcher(InvocationDispatcher invocationDispatcher) {
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

    public Provider<ConnectionPool> getConnectionPoolProvider() {
        return connectionPoolProvider;
    }

    @Inject
    public void setConnectionPoolProvider(Provider<ConnectionPool> connectionPoolProvider) {
        this.connectionPoolProvider = connectionPoolProvider;
    }

    @Inject
    public void setName(@Named(NAME) String name) {
        this.name = name;
        buildNodeIdIfPossible();
        logger = LoggerFactory.getLogger(loggerName());
    }

    public NodeLifecycle getNodeLifecycle() {
        return nodeLifecycle;
    }

    @Inject
    public void setNodeLifecycle(NodeLifecycle nodeLifecycle) {
        this.nodeLifecycle = nodeLifecycle;
    }

    private String loggerName() {
        return Stream.of(JeroMQNode.class.getName(), getNodeId().getApplicationUuid().toString())
                     .filter(s -> s != null)
                     .collect(Collectors.joining("."));
    }

    public UUID getInstanceUuid() {
        return instanceUuid;
    }

    @Inject
    @Named(LOCAL_INSTANCE_ID)
    public void setInstanceUuid(UUID instanceUuid) {
        this.instanceUuid = instanceUuid;
    }

    private class NodeContext {

        private final AtomicBoolean running = new AtomicBoolean();

        private final ConnectionPool outboundConnectionPool = getConnectionPoolProvider().get();

        private final CountDownLatch proxyStartupLatch = new CountDownLatch(1);

        final AtomicInteger dispatcherCount = new AtomicInteger();

        private final ExecutorService dispatchExecutorService = Executors.newCachedThreadPool(r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(format("%s.in #%d", getName(), dispatcherCount.incrementAndGet()));
            thread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));
            return thread;
        });

        private final Thread proxyThread;
        {
            proxyThread = new Thread(() -> bindFrontendSocketAndPerformWork());
            proxyThread.setDaemon(true);
            proxyThread.setName(JeroMQNode.this.getClass().getSimpleName() + " dispatch");
            proxyThread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));
        }

        public void start() {

            running.set(true);
            proxyThread.start();

            try {
                proxyStartupLatch.await();
            } catch (InterruptedException e) {
                throw new InternalException(e);
            }

            outboundConnectionPool.start(zc -> {
                final Socket socket = zc.createSocket(PUSH);
                socket.connect(getOutboundAddr());
                return socket;
            }, getName() + ".out");

        }

        public void stop() {

            outboundConnectionPool.stop();
            dispatchExecutorService.shutdown();

            running.set(false);
            proxyThread.interrupt();

            try {
                proxyThread.join();
            } catch (InterruptedException e) {
                logger.error("Interrupted while shutting down Node.", e);
            }

            try {
                if (!dispatchExecutorService.awaitTermination(10, MINUTES)) {
                    logger.error("Terminating dispatchers timed out.");
                }
            } catch (InterruptedException e) {
                logger.error("Interrupted while shutting down Node.", e);
            }

        }

        private void bindFrontendSocketAndPerformWork() {

            try (final ZContext context = ZContext.shadow(getzContext());
                 final Socket frontend = context.createSocket(ROUTER);
                 final Socket outbound = context.createSocket(PULL);
                 final Poller poller = context.createPoller(4)) {

                frontend.setRouterMandatory(true);
                frontend.bind(getBindAddress());

                outbound.bind(getOutboundAddr());

                final int frontendIndex = poller.register(frontend, POLLIN | POLLERR);
                final int outboundIndex = poller.register(outbound, POLLIN | POLLERR);

                proxyStartupLatch.countDown();
                logger.info("Started up.");

                while (running.get() && !interrupted()) {
                    try {
                        if (poller.poll(10000) < 0) {
                            logger.info("Poller signaled interruption.  Terminating frontend socket.");
                            break;
                        }

                        if (poller.pollin(frontendIndex)) {
                            final ZMsg msg = ZMsg.recvMsg(frontend);
                            dispatchExecutorService.submit(() -> dispatchMethodInvocation(msg));
                        } else if (poller.pollerr(frontendIndex)) {
                            logger.error("Error in frontend socket.");
                        }

                        if (poller.pollin(outboundIndex)) {
                            final ZMsg msg = ZMsg.recvMsg(outbound);
                            msg.send(frontend);
                        } else if (poller.pollerr(outboundIndex)) {
                            logger.error("Error in outbound socket.");
                        }
                    } catch (Exception ex) {
                        logger.error("Exception in main IO Thread.", ex);
                    }
                }

            }

        }

        private void dispatchMethodInvocation(final ZMsg msg) {

            final ZMsg identity = IdentityUtil.popIdentity(msg);

            final AtomicReference<Invocation> invocationAtomicReference = new AtomicReference<>();

            final RequestHeader requestHeader = new RequestHeader();
            requestHeader.getByteBuffer().put(msg.remove().getData());

            final AtomicBoolean sync = new AtomicBoolean();
            final AtomicInteger remaining = new AtomicInteger(requestHeader.additionalParts.get());

            final Consumer<InvocationError> syncInvocationErrorConsumer = invocationError -> {
                if (sync.getAndSet(true)) {
                    logger.error("Already set sync response.  Ignoring {}", invocationError);
                } else {
                    outboundConnectionPool.processV(outbound -> sendError(outbound.socket(), invocationError, 0, identity));
                }
            };

            final Consumer<InvocationError> asyncInvocationErrorConsumer = invocationError -> {
                if (remaining.getAndSet(0) <= 0) {
                    logger.error("Suppressing invocation error.  Already sent.", invocationError.getThrowable());
                } else {
                    outboundConnectionPool.processV(outbound -> sendError(outbound.socket(), invocationError, 1, identity));
                }
            };

            final Consumer<InvocationResult> syncInvocationResultConsumer = invocationResult -> {
                if (sync.getAndSet(true)) {
                    logger.error("Already set sync response.  Ignoring {}", invocationResult);
                } else {
                    outboundConnectionPool.processV(outbound -> sendResult(outbound.socket(), invocationResult, 0, identity, syncInvocationErrorConsumer));
                }
            };

            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList = range(0, requestHeader.additionalParts.get())
                .map(index -> index + 1)
                .mapToObj(part -> (Consumer<InvocationResult>) invocationResult -> {
                    if (remaining.getAndDecrement() <= 0) {
                        logger.debug("Ignoring invocation result {} because of previous errors.", invocationResult);
                    } else {
                        outboundConnectionPool.processV(outbound -> sendResult(outbound.socket(), invocationResult, part, identity, asyncInvocationErrorConsumer));
                    }
                }).collect(toList());

            try {

                final byte[] payload = msg.remove().getData();
                final Invocation invocation = getPayloadReader().read(Invocation.class, payload);
                invocationAtomicReference.set(invocation);

                getInvocationDispatcher().dispatch(
                    invocation,
                    syncInvocationResultConsumer, syncInvocationErrorConsumer,
                    asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);

                if (!sync.get()) {
                    logger.error("Neither return nor error callback was invoked.");
                }

            } catch (IOException e) {
                final InvocationError invocationError = new InvocationError();
                invocationError.setThrowable(e);
                asyncInvocationErrorConsumer.accept(invocationError);
            }

        }

        private void sendResult(final Socket socket,
                                final InvocationResult invocationResult,
                                final int part,
                                final ZMsg identity,
                                final Consumer<InvocationError> invocationErrorConsumer) {

            final ResponseHeader responseHeader = new ResponseHeader();
            responseHeader.type.set(MessageType.INVOCATION_RESULT);
            responseHeader.part.set(part);

            final byte[] responseHeaderBytes = new byte[responseHeader.size()];
            responseHeader.getByteBuffer().get(responseHeaderBytes);

            try {

                final byte[] payload = getPayloadWriter().write(invocationResult);
                final ZMsg msg = identity.duplicate();

                msg.addLast(EMPTY_DELIMITER);
                msg.addLast(responseHeaderBytes);
                msg.addLast(payload);

                msg.send(socket);

            } catch (IOException e) {
                logger.error("Could not write payload to byte stream.  Sending empty.", e);
                final InvocationError invocationError = new InvocationError();
                invocationError.setThrowable(e);
                sendError(socket, invocationError, part, identity);
            }

        }

        private void sendError(final Socket socket,
                               final InvocationError invocationError,
                               final int part,
                               final ZMsg identity) {

            final ResponseHeader responseHeader = new ResponseHeader();
            responseHeader.type.set(INVOCATION_ERROR);
            responseHeader.part.set(part);

            final byte[] responseHeaderBytes = new byte[responseHeader.size()];
            responseHeader.getByteBuffer().get(responseHeaderBytes);

            byte[] payload;

            try {
                payload = getPayloadWriter().write(invocationError);
            } catch (Exception e) {
                logger.error("Could not write payload to byte stream.  Sending empty payload.", e);
                payload = new byte[0];
            }

            final ZMsg msg = identity.duplicate();

            msg.addLast(EMPTY_DELIMITER);
            msg.addLast(responseHeaderBytes);
            msg.addLast(payload);

            msg.send(socket);

        }

    }

}

