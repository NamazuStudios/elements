package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZPoller;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.lang.Thread.interrupted;

public class JeroMQRemoteInvoker implements RemoteInvoker {

    public static final String NODE_ADDRESS = "com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker.nodeAddress";

    private static final Logger logger = LoggerFactory.getLogger(JeroMQRemoteInvoker.class);

    private final Set<LatchedFuture<Object>> futureSet =
        new ConcurrentHashMap<LatchedFuture<Object>, Object>()
        .keySet(new Object());

    private String nodeAddress;

    private PayloadReader payloadReader;

    private PayloadWriter payloadWriter;

    private ConnectionPool connectionPool;

    @Override
    public void start() {
        getConnectionPool().start(zContext -> {
            final ZMQ.Socket socket = zContext.createSocket(ZMQ.DEALER);
            socket.connect(getNodeAddress());
            return socket;
        }, JeroMQRemoteInvoker.class.getName());
    }

    @Override
    public void stop() {
        getConnectionPool().stop();
        futureSet.forEach(futureSet -> futureSet.cancel(true));
        futureSet.clear();
    }

    @Override
    public Future<Object> invoke(final Invocation invocation,
                                 final InvocationErrorConsumer invocationErrorConsumer,
                                 final List<Consumer<InvocationResult>> invocationResultConsumerList) {

        final LatchedFuture<Object> latchedFuture = new LatchedFuture<>();

        getConnectionPool().process((ConnectionPool.Connection connection) -> {

            try (final ZMQ.Poller poller = connection.context().createPoller(1)) {

                send(connection.socket(), invocation, invocationResultConsumerList.size());
                final int sIndex = poller.register(connection.socket(), ZPoller.READABLE);

                final int expectedResponseCount = 1 + invocationResultConsumerList.size();

                for (int received = 0; received < expectedResponseCount && !interrupted(); ++received) {

                    if (!pollForResponse(poller, sIndex)) {
                        break;
                    }

                    final ResponseHeader responseHeader = receiveHeader(connection.socket());

                    final boolean success = handleResponse(connection.socket(),
                                                           latchedFuture,
                                                           responseHeader,
                                                           invocationErrorConsumer,
                                                           result -> latchedFuture.setResultCallable(() -> result.getResult()),
                                                           invocationResultConsumerList);

                    if (!success) {
                        break;
                    }

                }

                logger.info("Finished invocation");

            } catch (Throwable th) {

                logger.error("Suppressing error because return value was already processed.", th);

                final boolean set = latchedFuture.setResultCallable(() -> {
                    logger.error("Could not dispatch invocation.", th);
                    final InvocationError invocationError = new InvocationError();
                    invocationError.setThrowable(th);
                    invocationErrorConsumer.accept(invocationError);
                    throw th;
                });

                if (!set) {
                    logger.error("Already set result.  Silencing error.");
                }

                throw th;

            }

        });

        futureSet.add(latchedFuture);
        return latchedFuture;

    }

    private boolean pollForResponse(final ZMQ.Poller poller, final int sIndex) {
        while (poller.poll(1000) == 0 && !interrupted());
        return poller.pollin(sIndex);
    }

    private void send(final ZMQ.Socket socket, final Invocation invocation, final int additionalCount) {

        final RequestHeader requestHeader = new RequestHeader();
        requestHeader.additionalParts.set(additionalCount);

        final byte[] payload;

        try {
            payload = getPayloadWriter().write(invocation);
        } catch (IOException e) {
            throw new InternalException(e);
        }

        socket.sendByteBuffer(requestHeader.getByteBuffer(), ZMQ.SNDMORE);
        socket.send(payload);

    }

    private boolean handleResponse(final ZMQ.Socket socket,
                                   final LatchedFuture<?> latchedFuture,
                                   final ResponseHeader responseHeader,
                                   final InvocationErrorConsumer invocationErrorConsumer,
                                   final Consumer<InvocationResult> resultObjectCallable,
                                   final List<Consumer<InvocationResult>> invocationResultConsumerList) {

        final int part = responseHeader.part.get();

        switch (responseHeader.type.get()) {
            case INVOCATION_ERROR:
                handleError(socket, latchedFuture, invocationErrorConsumer);
                return false;
            case INVOCATION_RESULT:
                handleResult(socket, part == 0 ? resultObjectCallable : invocationResultConsumerList.get(part - 1));
                return true;
            default:
                logger.error("Invalid response type {}", responseHeader.type.get());
                throw new InternalException("Invalid response type " + responseHeader.type.get());
        }

    }

    private void handleError(final ZMQ.Socket socket,
                             final LatchedFuture<?> latchedFuture,
                             final InvocationErrorConsumer invocationErrorConsumer) {

        final byte[] bytes = socket.recv();
        final InvocationError invocationError;

        try {
            invocationError = getPayloadReader().read(InvocationError.class, bytes);
        } catch (IOException e) {
            throw new InternalException(e);
        }

        try {
            invocationErrorConsumer.accept(invocationError);
        } catch (Throwable th) {
            if (!latchedFuture.setResultCallable(() -> { throw th; })) {
                logger.error("Already set result.  Silencing error.", th);
            }
        }

    }

    private void handleResult(final ZMQ.Socket socket, final Consumer<InvocationResult> invocationResultConsumer) {

        final byte[] bytes = socket.recv();
        final InvocationResult invocationResult;

        try {
            invocationResult = getPayloadReader().read(InvocationResult.class, bytes);
        } catch (IOException e) {
            throw new InternalException(e);
        }

        invocationResultConsumer.accept(invocationResult);

    }

    private ResponseHeader receiveHeader(final ZMQ.Socket socket) {
        final ResponseHeader responseHeader = new ResponseHeader();
        socket.recvByteBuffer(responseHeader.getByteBuffer(), 0);
        return responseHeader;
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

    public String getNodeAddress() {
        return nodeAddress;
    }

    @Inject
    public void setNodeAddress(@Named(NODE_ADDRESS) String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    @Inject
    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    /**
     * A {@link Future<T>} type which will wait for a call from another thread before returning the value.  This is
     * backed by a {@link CountDownLatch} (hence the name) as well as a {@link FutureTask} to handle the result.  This
     * allows for the setting of a {@link Callable<T>} to supply the result which will not get called until the latch
     * has counted down.
     *
     * By default this supplies {@link Callable<T>} which just throws an exception.  Only one result can be set to this
     * instance.
     *
     * @param <T>
     */
    private class LatchedFuture<T> implements Future<T> {

        private final ResultSupplier<T> canceled = () -> {
            throw new IllegalStateException("Operation was canceled.");
        };

        private final ResultSupplier<T> unspecifiedResult = () -> {
            throw new InternalException("Interrupted or request timed out.");
        };

        private final AtomicInteger state = new AtomicInteger();

        private final AtomicReference<ResultSupplier<T>> resultCallable = new AtomicReference<>(unspecifiedResult);

        private final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            try {
                return resultCallable.compareAndSet(unspecifiedResult, canceled);
            } finally {
                latch.countDown();
            }
        }

        @Override
        public boolean isCancelled() {
            return resultCallable.get() == canceled;
        }

        @Override
        public boolean isDone() {
            return resultCallable.get() != unspecifiedResult;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {

            latch.await();

            try {
                return resultCallable.get().supply();
            } catch (Throwable e) {
                throw new ExecutionException(e);
            }

        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

            latch.await(timeout, unit);

            try {
                return resultCallable.get().supply();
            } catch (Throwable e) {
                throw new ExecutionException(e);
            }

        }

        /**
         * Sets the result {@link Callable<T>} and releases the latch allowing the supplied result to be available
         * immediately after this call returns.  If a result has previously been set, then this simply rejects the
         * changed value and does nothing.
         *
         * @param resultCallable the result {@link Callable<T>}
         * @return true if the result was set, or false if a previous result was alredy set.
         */
        public boolean setResultCallable(final ResultSupplier<T> resultCallable) {
            if (this.resultCallable.compareAndSet(unspecifiedResult, resultCallable)) {
                futureSet.remove(this);
                latch.countDown();
                return true;
            } else {
                return false;
            }
        }

    }

    @FunctionalInterface
    private interface ResultSupplier<T> {

        T supply() throws Throwable;

    }

}
