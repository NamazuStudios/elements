package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceBinding;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMsg;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.remote.MessageType.INVOCATION_ERROR;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.EMPTY_DELIMITER;
import static java.lang.String.format;
import static java.lang.Thread.interrupted;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.zeromq.SocketType.PULL;
import static org.zeromq.SocketType.PUSH;
import static org.zeromq.SocketType.ROUTER;
import static org.zeromq.ZMQ.*;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;

public class JeroMQNode implements Node {

    private static final Logger staticLogger = LoggerFactory.getLogger(JeroMQNode.class);

    private static final int POLL_TIMEOUT = 1000;

    private static final String OUTBOUND_ADDR_FORMAT = "inproc://node/%s/out";

    private final AtomicReference<NodeContext> context = new AtomicReference<>();

    private String name;

    private NodeId nodeId;

    private ZContext zContext;

    private LocalInvocationDispatcher invocationDispatcher;

    private PayloadReader payloadReader;

    private PayloadWriter payloadWriter;

    private Provider<ConnectionPool> connectionPoolProvider;

    private NodeLifecycle nodeLifecycle;

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

    public ZContext getzContext() {
        return zContext;
    }

    @Inject
    public void setzContext(ZContext zContext) {
        this.zContext = zContext;
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

    public Provider<ConnectionPool> getConnectionPoolProvider() {
        return connectionPoolProvider;
    }

    @Inject
    public void setConnectionPoolProvider(Provider<ConnectionPool> connectionPoolProvider) {
        this.connectionPoolProvider = connectionPoolProvider;
    }

    @Inject
    public void setNodeId(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    @Inject
    public void setName(@Named(NAME) String name) {
        this.name = name;
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

        private final AtomicBoolean running = new AtomicBoolean();

        private final ConnectionPool outboundConnectionPool = getConnectionPoolProvider().get();

        private final CountDownLatch proxyStartupLatch = new CountDownLatch(1);

        private final AtomicInteger dispatcherCount = new AtomicInteger();

        private final ExecutorService dispatchExecutorService = Executors.newCachedThreadPool(r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(format("%s %s.in #%d", getClass().getSimpleName(), getName(), dispatcherCount.incrementAndGet()));
            thread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));
            return thread;
        });

        private Thread proxyThread;

        public void start(final InstanceBinding instanceBinding) {

            proxyThread = new Thread(() -> bindFrontendSocketAndPerformWork(instanceBinding));
            proxyThread.setDaemon(true);
            proxyThread.setName(format("%s %s %s dispatch.",
                JeroMQNode.this.getClass().getSimpleName(),
                getName(),
                getNodeId().asString()));
            proxyThread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));

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

        private void bindFrontendSocketAndPerformWork(final InstanceBinding instanceBinding) {

            try (final ZContext context = ZContext.shadow(getzContext());
                 final Socket frontend = context.createSocket(ROUTER);
                 final Socket outbound = context.createSocket(PULL);
                 final Poller poller = context.createPoller(4)) {

                frontend.setRouterMandatory(true);
                frontend.bind(instanceBinding.getBindAddress());

                outbound.bind(getOutboundAddr());

                final int frontendIndex = poller.register(frontend, POLLIN | POLLERR);
                final int outboundIndex = poller.register(outbound, POLLIN | POLLERR);

                proxyStartupLatch.countDown();
                logger.info("Started up.");

                while (running.get()) {
                    try {

                        if (!running.get() || poller.poll(POLL_TIMEOUT) < 0) {
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

            } finally {
                logger.info("Node proxy thread exiting.");
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
            } finally {

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

