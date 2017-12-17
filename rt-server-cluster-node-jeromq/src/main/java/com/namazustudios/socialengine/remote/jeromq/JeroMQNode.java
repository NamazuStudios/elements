package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.Node;
import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.util.FinallyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.remote.MessageType.INVOCATION_ERROR;
import static com.namazustudios.socialengine.rt.util.FinallyAction.with;
import static java.lang.Thread.interrupted;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.zeromq.ZMQ.DEALER;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMQ.SNDMORE;
import static org.zeromq.ZMQ.poll;

public class JeroMQNode implements Node {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQNode.class);

    private static final String INPROC_BIND_ADDR = "inproc://JeroMQNode-dispatch";

    public static final String BIND_ADDRESS = "com.namazustudios.socialengine.remote.jeromq.JeroMQNode.bindAddress";

    public static final String NUMBER_OF_DISPATCHERS = "com.namazustudios.socialengine.remote.jeromq.JeroMQNode.numberOfDispatchers";

    private final AtomicReference<Context> context = new AtomicReference<>();

    private ZContext zContext;

    private String  bindAddress;

    private int numberOfDispachers;

    private InvocationDispatcher invocationDispatcher;

    private PayloadReader payloadReader;

    private PayloadWriter payloadWriter;

    private Provider<ConnectionPool> connectionPoolProvider;

    @Override
    public void start() {

        final Context c = new Context();

        if (context.compareAndSet(null, c)) {
            c.stop();
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    @Override
    public void stop() {

        final Context c = context.get();

        if (context.compareAndSet(c, null)) {
            c.start();
        } else {
            throw new IllegalStateException("Already stopped.");
        }

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

    private class Context {

        private final AtomicBoolean running = new AtomicBoolean();

        private final ConnectionPool connectionPool = getConnectionPoolProvider().get();

        private final CountDownLatch proxyStartupLatch = new CountDownLatch(1);

        private final Thread proxyThread;
        {

            proxyThread = new Thread(() -> {
                bindFrontendSocketAndPerformWork();
                proxyStartupLatch.countDown();
            });

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

            connectionPool.start(zc -> {
                final ZMQ.Socket socket = zc.createSocket(DEALER);
                socket.connect(INPROC_BIND_ADDR);
                return socket;
            });

            final int toDispatch = getNumberOfDispachers();

            for (int i = 0; i < toDispatch; ++i) {
                connectionPool.process(connection -> connectBackendAndDispatchInvocations(connection.socket()));
            }

        }

        public void stop() {

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

            try (final ZMQ.Socket frontend = getzContext().createSocket(ZMQ.ROUTER);
                 final ZMQ.Socket backend = getzContext().createSocket(ZMQ.DEALER);
                 final ZMQ.Poller poller = getzContext().createPoller(2)) {

                actions = with(() -> getzContext().destroySocket(frontend))
                          .then(() -> getzContext().destroySocket(backend));

                frontend.bind(getBindAddress());
                backend.bind(INPROC_BIND_ADDR);

                final int fIndex = poller.register(frontend, POLLIN);
                final int bIndex = poller.register(backend, POLLIN);

                while (running.get() && !interrupted()) {

                    if (poller.poll(1000) == 0) {
                        continue;
                    }

                    if (poller.pollin(fIndex)) {
                        final ZMsg msg = ZMsg.recvMsg(frontend);
                        msg.send(backend);
                    }

                    if (poller.pollin(bIndex)) {
                        final ZMsg msg = ZMsg.recvMsg(backend);
                        msg.send(frontend);
                    }

                }

            } finally {
                actions.perform();
            }
        }

        private void connectBackendAndDispatchInvocations(final ZMQ.Socket socket) {
            try (final ZMQ.Poller poller = getzContext().createPoller(1)) {

                final int index = poller.register(socket, POLLIN);

                while (running.get() && !interrupted()) {
                    if (poller.poll(1000) > 0 && poller.pollin(index)) {
                        dispatchMethodInvocation(socket);
                    }
                }

            }
        }

        private void dispatchMethodInvocation(final ZMQ.Socket inbound) {

            final byte[] identity = inbound.recv();
            final byte[] delimiter = inbound.recv();

            final RequestHeader requestHeader = new RequestHeader();
            inbound.recvByteBuffer(requestHeader.getByteBuffer(), 0);

            if (delimiter.length > 0) {
                throw new InternalException("Invalid delimiter " + Arrays.toString(delimiter));
            }

            final AtomicInteger remaining = new AtomicInteger(1 + requestHeader.additionalParts.get());

            final Consumer<InvocationError> invocationErrorConsumer = invocationError -> {
                connectionPool.process(outbound -> {

                    if (remaining.getAndSet(0) > 0) {

                        final ResponseHeader responseHeader = new ResponseHeader();
                        responseHeader.type.set(INVOCATION_ERROR);
                        responseHeader.part.set(0);

                        byte[] payload;

                        try {
                            payload = getPayloadWriter().write(invocationError);
                        } catch (IOException e) {
                            logger.error("Could not write payload to byte stream.  Sending empty payload.", e);
                            payload = new byte[0];
                        }

                        outbound.socket().send(identity, SNDMORE);
                        outbound.socket().send(delimiter, SNDMORE);
                        outbound.socket().sendByteBuffer(responseHeader.getByteBuffer(), SNDMORE);
                        outbound.socket().send(payload);

                    } else {
                        logger.info("Ignoring other invocation errors.  Terminating context.");
                    }

                });

            };

            final Consumer<InvocationResult> returnInvocationResultConsumer = invocationResult -> {
                connectionPool.process(outbound -> {
                    if (remaining.decrementAndGet() <= 0) {
                        logger.info("Ignoring invocation result {} because of previous errors.", invocationResult);
                    } else {
                        sendResult(inbound, invocationResult, 0, identity, delimiter, invocationErrorConsumer);
                    }
                });
            };

            final List<Consumer<InvocationResult>> additionalInvocationResultConsumerList =
                range(0, requestHeader.additionalParts.get())
                .map(index -> index + 1)
                .mapToObj(part -> (Consumer<InvocationResult>) invocationResult -> connectionPool.process(outbound -> {
                    if (remaining.decrementAndGet() <= 0) {
                        logger.info("Ignoring invocation result {} because of previous errors.", invocationResult);
                    } else {
                        sendResult(outbound.socket(), invocationResult, part, identity, delimiter, invocationErrorConsumer);
                    }
                })).collect(toList());

            try {

                final Invocation invocation = getPayloadReader().read(Invocation.class, inbound.recv());

                getInvocationDispatcher().dispatch(invocation,
                        invocationErrorConsumer,
                        returnInvocationResultConsumer,
                        additionalInvocationResultConsumerList);

            } catch (IOException e) {
                final InvocationError invocationError = new InvocationError();
                invocationError.setThrowable(e);
                invocationErrorConsumer.accept(invocationError);
            }

        }

        private void sendResult(final ZMQ.Socket socket,
                                final InvocationResult invocationResult,
                                final int part,
                                final byte[] identity,
                                final byte[] delimiter,
                                final Consumer<InvocationError> invocationErrorConsumer) {

            final ResponseHeader responseHeader = new ResponseHeader();
            responseHeader.type.set(MessageType.INVOCATION_RESULT);
            responseHeader.part.set(part);

            try {
                final byte[] payload = getPayloadWriter().write(invocationResult);
                socket.send(identity, SNDMORE);
                socket.send(delimiter, SNDMORE);
                socket.sendByteBuffer(responseHeader.getByteBuffer(), SNDMORE);
                socket.send(payload);
            } catch (IOException e) {
                logger.error("Could not write payload to byte stream.  Sending empty.", e);
                final InvocationError invocationError = new InvocationError();
                invocationError.setThrowable(e);
                invocationErrorConsumer.accept(invocationError);
            }

        }

    }

}
