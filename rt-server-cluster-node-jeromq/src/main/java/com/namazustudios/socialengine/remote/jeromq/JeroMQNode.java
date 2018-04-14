package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.jeromq.Identity;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.util.FinallyAction;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.jeromq.Identity.EMPTY_DELIMITER;
import static com.namazustudios.socialengine.rt.remote.MessageType.INVOCATION_ERROR;
import static com.namazustudios.socialengine.rt.util.FinallyAction.with;
import static java.lang.String.format;
import static java.lang.Thread.interrupted;
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

    public static final String NUMBER_OF_DISPATCHERS = "com.namazustudios.socialengine.remote.jeromq.JeroMQNode.numberOfDispatchers";

    private final AtomicReference<Context> context = new AtomicReference<>();

    private String id;

    private String name;

    private Identity identity;

    private ZContext zContext;

    private String  bindAddress;

    private int numberOfDispachers;

    private InvocationDispatcher invocationDispatcher;

    private PayloadReader payloadReader;

    private PayloadWriter payloadWriter;

    private Provider<ConnectionPool> connectionPoolProvider;

    private Logger logger = staticLogger;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getInboundAddr() {
        return format(INBOUND_ADDR_FORMAT, getId());
    }

    public String getOutboundAddr() {
        return format(OUTBOUND_ADDR_FORMAT, getId());
    }

    @Override
    public void start() {

        final Context c = new Context();

        if (context.compareAndSet(null, c)) {
            logger.info("Starting up.");
            c.start();
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    @Override
    public void stop() {

        final Context c = context.get();

        if (context.compareAndSet(c, null)) {
            logger.info("Shutting down.");
            c.stop();
        } else {
            throw new IllegalStateException("Already stopped.");
        }

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

    public String getBindAddress() {
        return bindAddress;
    }

    @Inject
    public void setBindAddress(@Named(BIND_ADDRESS) String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public int getNumberOfDispachers() {
        return numberOfDispachers;
    }

    @Inject
    public void setNumberOfDispachers(@Named(NUMBER_OF_DISPATCHERS) int numberOfDispachers) {
        this.numberOfDispachers = numberOfDispachers;
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
    public void setId(@Named(ID) String id) {
        this.id = id;
        logger = LoggerFactory.getLogger(loggerName());
    }

    @Inject
    public void setName(@Named(NAME) String name) {
        this.name = name;
        logger = LoggerFactory.getLogger(loggerName());
    }

    private String loggerName() {
        return Stream.of(JeroMQNode.class.getName(), getName(), getId())
                     .filter(s -> s != null)
                     .collect(Collectors.joining("."));
    }

    private class Context {

        private final AtomicBoolean running = new AtomicBoolean();

        private final ConnectionPool inboundConnectionPool = getConnectionPoolProvider().get();

        private final ConnectionPool outboundConnectionPool = getConnectionPoolProvider().get();

        private final CountDownLatch proxyStartupLatch = new CountDownLatch(1);

        private final Thread proxyThread;
        {
            proxyThread = new Thread(() -> bindFrontendSocketAndPerformWork());
            proxyThread.setDaemon(true);
            proxyThread.setName(JeroMQNode.this.getClass().getSimpleName() + " dispatcher thread.");
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

            inboundConnectionPool.start(zc -> {
                final Socket socket = zc.createSocket(PULL);
                socket.connect(getInboundAddr());
                return socket;
            }, getName() + ".in");

            final int toDispatch = getNumberOfDispachers();

            for (int i = 0; i < toDispatch; ++i) {
                inboundConnectionPool.processV(connection -> {
                    try (final Poller poller = getzContext().createPoller(1)) {

                        final int index = poller.register(connection.socket(), POLLIN | POLLERR);

                        while (running.get() && !interrupted()) {
                            if (poller.poll(5000) < 0) {
                                logger.info("Poller signaled interruption.  Terminating inbound connection.");
                                break;
                            } else if (poller.pollin(index)) {
                                dispatchMethodInvocation(connection.socket());
                            }
                        }

                    } finally {
                        logger.info("Terminating inbound connection.");
                    }

                });
            }

        }

        public void stop() {

            inboundConnectionPool.stop();
            outboundConnectionPool.stop();

            running.set(false);
            proxyThread.interrupt();

            try {
                proxyThread.join();
            } catch (InterruptedException e) {
                logger.error("Interrupted while shutting down Node.", e);
                throw new InternalException(e);
            }

        }

        private void bindFrontendSocketAndPerformWork() {

            FinallyAction actions = () -> {};

            try (final Socket frontend = getzContext().createSocket(ROUTER);
                 final Socket inbound = getzContext().createSocket(PUSH);
                 final Socket outbound = getzContext().createSocket(PULL);
                 final Poller poller = getzContext().createPoller(4)) {

                actions = with(() -> getzContext().destroySocket(frontend))
                          .then(() -> getzContext().destroySocket(inbound))
                          .then(() -> getzContext().destroySocket(outbound));

                frontend.setRouterMandatory(true);
                frontend.bind(getBindAddress());

                inbound.bind(getInboundAddr());
                outbound.bind(getOutboundAddr());

                final int frontendIndex = poller.register(frontend, POLLIN | POLLERR);
                final int outboundIndex = poller.register(outbound, POLLIN | POLLERR);

                proxyStartupLatch.countDown();
                logger.info("Started up.");

                while (running.get() && !interrupted()) {

                    if (poller.poll(5000) < 0) {
                        logger.error("Poller signaled interruption.  Terminating frontend socket.");
                        break;
                    }

                    if (poller.pollin(frontendIndex)) {
                        final ZMsg msg = ZMsg.recvMsg(frontend);
                        msg.send(inbound);
                    } else if (poller.pollerr(frontendIndex)) {
                        logger.error("Error in frontend socket.");
                    }

                    if (poller.pollin(outboundIndex)) {
                        final ZMsg msg = ZMsg.recvMsg(outbound);
                        msg.send(frontend);
                    } else if (poller.pollerr(outboundIndex)) {
                        logger.error("Error in outbound socket.");
                    }

                }

            } finally {
                actions.perform();
            }
        }

        private void dispatchMethodInvocation(final Socket inbound) {

            final ZMsg msg = ZMsg.recvMsg(inbound);
            final ZMsg identity = getIdentity().popIdentity(msg);

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
                        logger.info("Ignoring invocation result {} because of previous errors.", invocationResult);
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

