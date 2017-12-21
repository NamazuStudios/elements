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
import org.zeromq.ZMonitor;
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

import static com.namazustudios.socialengine.rt.remote.MessageType.INVOCATION_ERROR;
import static com.namazustudios.socialengine.rt.util.FinallyAction.with;
import static java.lang.Thread.interrupted;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.zeromq.ZMQ.*;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMQ.Poller.POLLOUT;

public class JeroMQNode implements Node {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQNode.class);

    private static final String INBOUND_ADDR = "inproc://node.in";

    private static final String OUTBOUND_ADDR = "inproc://node.out";

    private static final String MONITORING_IN = "inproc://node.in.mon";

    private static final String MONITORING_OUT = "inproc://node.out.mon";

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
            c.start();
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    @Override
    public void stop() {

        final Context c = context.get();

        if (context.compareAndSet(c, null)) {
            c.stop();
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
                socket.connect(OUTBOUND_ADDR);
                return socket;
            }, JeroMQNode.class.getSimpleName() + ".out");

            inboundConnectionPool.start(zc -> {
                final Socket socket = zc.createSocket(PULL);
                socket.connect(INBOUND_ADDR);
                return socket;
            }, JeroMQNode.class.getSimpleName() + ".out");

            final int toDispatch = getNumberOfDispachers();

            for (int i = 0; i < toDispatch; ++i) {
                inboundConnectionPool.process(connection -> {
                    try (final Poller poller = getzContext().createPoller(1)) {

                        final int index = poller.register(connection.socket(), POLLIN | POLLERR);

                        while (running.get() && !interrupted()) {

                            if (poller.poll(1000) == 0) {
                                continue;
                            }

                            if (poller.pollin(index)) {
                                dispatchMethodInvocation(connection.socket());
                            }

                        }

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
                 final Socket inMonitor = getzContext().createSocket(PAIR);

                 final Socket outbound = getzContext().createSocket(PULL);
                 final Socket outMonitor = getzContext().createSocket(PAIR);

                 final Poller poller = getzContext().createPoller(4)) {

                actions = with(() -> getzContext().destroySocket(frontend))
                          .then(() -> getzContext().destroySocket(inbound))
                          .then(() -> getzContext().destroySocket(inMonitor))
                          .then(() -> getzContext().destroySocket(outbound))
                          .then(() -> getzContext().destroySocket(outMonitor));

                frontend.bind(getBindAddress());

                inbound.monitor(MONITORING_IN, ZMQ.EVENT_ALL);
                inMonitor.connect(MONITORING_IN);

                outbound.monitor(MONITORING_OUT, ZMQ.EVENT_ALL);
                outMonitor.connect(MONITORING_OUT);

                inbound.bind(INBOUND_ADDR);
                outbound.bind(OUTBOUND_ADDR);

                final int frontendIndex = poller.register(frontend, POLLIN | POLLERR);
                final int outboundIndex = poller.register(outbound, POLLIN | POLLERR);
                final int inMonitorIndex = poller.register(inMonitor, POLLIN | POLLERR);
                final int outMonitorIndex = poller.register(outMonitor, POLLIN | POLLERR);

                proxyStartupLatch.countDown();

                while (running.get() && !interrupted()) {

                    if (poller.poll(1000) == 0) {
                        continue;
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

                    if (poller.pollin(inMonitorIndex)) {
                        final ZMsg msg = ZMsg.recvMsg(inMonitor);
                        logger.info("Inbound {} {} {}", msg.popString(), msg.popString(), msg.popString());
                    } else if (poller.pollerr(inMonitorIndex)) {
                        logger.error("Error in inbound monitor socket.");
                    }

                    if (poller.pollin(outMonitorIndex)) {
                        final ZMsg msg = ZMsg.recvMsg(outMonitor);
                        logger.info("Outbound {} {} {}", msg.popString(), msg.popString(), msg.popString());
                    } else if (poller.pollerr(outMonitorIndex)) {
                        logger.error("Error in outbound monitor socket.");
                    }

                }

            } finally {
                actions.perform();
            }
        }

        private void dispatchMethodInvocation(final Socket inbound) {

            final ZMsg msg = ZMsg.recvMsg(inbound);
            final byte[] identity = msg.remove().getData();

            final AtomicReference<Invocation> invocationAtomicReference = new AtomicReference<>();

            final RequestHeader requestHeader = new RequestHeader();
            requestHeader.getByteBuffer().put(msg.remove().getData());

            final AtomicInteger remaining = new AtomicInteger(1 + requestHeader.additionalParts.get());

            final Consumer<InvocationError> invocationErrorConsumer = invocationError -> {
                if (remaining.getAndSet(0) > 0) {

                    final ResponseHeader responseHeader = new ResponseHeader();
                    responseHeader.type.set(INVOCATION_ERROR);
                    responseHeader.part.set(0);

                    outboundConnectionPool.process(outbound -> {

                        byte[] payload;

                        try {
                            payload = getPayloadWriter().write(invocationError);
                        } catch (IOException e) {
                            logger.error("Could not write payload to byte stream.  Sending empty payload.", e);
                            payload = new byte[0];
                        }

                        outbound.socket().send(identity, SNDMORE);
                        outbound.socket().sendByteBuffer(responseHeader.getByteBuffer(), SNDMORE);
                        outbound.socket().send(payload);

                    });

                } else {
                    logger.info("Ignoring other invocation errors.  Terminating context.");
                }

            };

            final Consumer<InvocationResult> returnInvocationResultConsumer = invocationResult -> {
                if (remaining.getAndDecrement() <= 0) {
                    logger.info("Ignoring invocation result {} because of previous errors.", invocationResult);
                } else {
                    outboundConnectionPool.process(outbound ->
                        sendResult(outbound.socket(), invocationResult, 0, identity, invocationErrorConsumer));
                }
            };


            final List<Consumer<InvocationResult>> additionalInvocationResultConsumerList =
                range(0, requestHeader.additionalParts.get())
                .map(index -> index + 1)
                .mapToObj(part -> (Consumer<InvocationResult>) invocationResult -> {
                    if (remaining.getAndDecrement() <= 0) {
                        logger.info("Ignoring invocation result {} because of previous errors.", invocationResult);
                    } else {
                        outboundConnectionPool.process(outbound ->
                            sendResult(outbound.socket(), invocationResult, part, identity, invocationErrorConsumer));
                    }
                }).collect(toList());

            try {

                final byte[] payload = msg.remove().getData();
                final Invocation invocation = getPayloadReader().read(Invocation.class, payload);
                invocationAtomicReference.set(invocation);

                getInvocationDispatcher().dispatch(
                        invocation,
                        invocationErrorConsumer,
                        returnInvocationResultConsumer,
                        additionalInvocationResultConsumerList);

            } catch (IOException e) {
                final InvocationError invocationError = new InvocationError();
                invocationError.setThrowable(e);
                invocationErrorConsumer.accept(invocationError);
            }

        }

        private void sendResult(final Socket socket,
                                final InvocationResult invocationResult,
                                final int part,
                                final byte[] identity,
                                final Consumer<InvocationError> invocationErrorConsumer) {

            final ResponseHeader responseHeader = new ResponseHeader();
            responseHeader.type.set(MessageType.INVOCATION_RESULT);
            responseHeader.part.set(part);

            try {
                final byte[] payload = getPayloadWriter().write(invocationResult);
                socket.send(identity, SNDMORE);
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
