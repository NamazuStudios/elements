package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZPoller;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.jeromq.Identity.EMPTY_DELIMITER;
import static java.lang.Thread.interrupted;
import static org.zeromq.ZMQ.SNDMORE;

public class JeroMQRemoteInvoker implements RemoteInvoker {

    public static final String CONNECT_ADDRESS = "com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker.connectAddress";

    private static final Logger logger = LoggerFactory.getLogger(JeroMQRemoteInvoker.class);

    private String connectAddress;

    private PayloadReader payloadReader;

    private PayloadWriter payloadWriter;

    private ConnectionPool connectionPool;

    @Override
    public void start() {
        getConnectionPool().start(zContext -> {
            final ZMQ.Socket socket = zContext.createSocket(ZMQ.DEALER);
            socket.connect(getConnectAddress());
            return socket;
        }, JeroMQRemoteInvoker.class.getName());
    }

    @Override
    public void stop() {
        getConnectionPool().stop();
    }

    @Override
    public Future<Object> invoke(final Invocation invocation,
                                 final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList, final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final RemoteInvocationFutureTask<Object> remoteInvocationFutureTask = new RemoteInvocationFutureTask<>();

        getConnectionPool().processV((ConnectionPool.Connection connection) -> {

            try (final ZMQ.Poller poller = connection.context().createPoller(1)) {

                send(connection.socket(), invocation, asyncInvocationResultConsumerList.size());
                final int sIndex = poller.register(connection.socket(), ZPoller.READABLE);

                final int expectedResponseCount = asyncInvocationResultConsumerList.size();

                boolean syncCompleted = false;
                boolean asyncCompleted = false;

                for (int remaining = expectedResponseCount; !(syncCompleted && asyncCompleted);) {

                    if (!pollForResponse(poller, sIndex)) {
                        break;
                    }

                    final ZMsg msg = ZMsg.recvMsg(connection.socket());
                    msg.pop();

                    final HandleResult result = handleResponse(
                        msg,
                        remoteInvocationFutureTask,
                        asyncInvocationResultConsumerList,
                        asyncInvocationErrorConsumer);

                    switch (result) {
                        case SYNC_ERROR:
                        case SYNC_RESULT:
                            syncCompleted = true;
                            break;
                        case ASYNC_RESULT:
                            if (!asyncCompleted && (--remaining) == 0) {
                                asyncCompleted = true;
                            }
                            break;
                        case ASYNC_ERROR:
                            asyncCompleted = true;
                            break;
                    }

                }

                logger.info("Finished Invocation.");

            }

        });

        return remoteInvocationFutureTask;

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

        socket.send(EMPTY_DELIMITER, SNDMORE);
        socket.sendByteBuffer(requestHeader.getByteBuffer(), SNDMORE);
        socket.send(payload);

    }

    private HandleResult handleResponse(final ZMsg msg,
                                        final RemoteInvocationFutureTask<Object> remoteInvocationFutureTask,
                                        final List<Consumer<InvocationResult>> asyncResultConsumerList,
                                        final InvocationErrorConsumer asyncErrorConsumer) {

        final ResponseHeader responseHeader = receiveHeader(msg);
        final int part = responseHeader.part.get();

        switch (responseHeader.type.get()) {
            case INVOCATION_RESULT:
                handleResult(msg, responseHeader, remoteInvocationFutureTask, asyncResultConsumerList);
                return part == 0 ? HandleResult.SYNC_RESULT : HandleResult.ASYNC_RESULT;
            case INVOCATION_ERROR:
                handleError(msg, responseHeader, remoteInvocationFutureTask, asyncErrorConsumer);
                return part == 0 ? HandleResult.SYNC_ERROR : HandleResult.ASYNC_ERROR;
            default:
                logger.error("Invalid response type {}", responseHeader.type.get());
                throw new InternalException("Invalid response type " + responseHeader.type.get());
        }

    }

    private void handleError(final ZMsg msg,
                             final ResponseHeader responseHeader,
                             final RemoteInvocationFutureTask<Object> remoteInvocationFutureTask,
                             final InvocationErrorConsumer asyncErrorConsumer) {

        final InvocationError invocationError;

        try {
            final byte[] bytes = msg.pop().getData();
            invocationError = getPayloadReader().read(InvocationError.class, bytes);
        } catch (IOException e) {
            throw new InternalException(e);
        }

        final int part = responseHeader.part.get();

        if (part == 0) {

            final Throwable throwable = invocationError.getThrowable();

            final Exception exception = throwable instanceof Exception ?
                (Exception) throwable :
                new RemoteInvocationException(throwable);

            if (!remoteInvocationFutureTask.setException(exception)) {
                logger.error("Already set result.  Silencing error.", exception);
            }

        } else if (part == 1) {
            asyncErrorConsumer.accept(invocationError);
        } else {
            throw new InternalException("Invalid error part " + responseHeader.part.get());
        }

    }


    private void handleResult(final ZMsg msg,
                              final ResponseHeader responseHeader,
                              final RemoteInvocationFutureTask<Object> remoteInvocationFutureTask,
                              final List<Consumer<InvocationResult>> asyncResultConsumerList) {

        final InvocationResult invocationResult;

        try {
            final byte[] bytes = msg.pop().getData();
            invocationResult = getPayloadReader().read(InvocationResult.class, bytes);
        } catch (IOException e) {
            throw new InternalException(e);
        }

        final int part = responseHeader.part.get();

        if (part == 0) {
            remoteInvocationFutureTask.setResult(invocationResult.getResult());
        } else {
            asyncResultConsumerList.get(part - 1).accept(invocationResult);
        }

    }

    private ResponseHeader receiveHeader(final ZMsg msg) {
        final ResponseHeader responseHeader = new ResponseHeader();
        responseHeader.getByteBuffer().put(msg.pop().getData());
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

    public String getConnectAddress() {
        return connectAddress;
    }

    @Inject
    public void setConnectAddress(@Named(CONNECT_ADDRESS) String connectAddress) {
        this.connectAddress = connectAddress;
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
    private class RemoteInvocationFutureTask<T> implements RunnableFuture<T> {

        private final ResultSupplier<T> unspecifiedResult = () -> {
            throw new InternalException("Interrupted or request timed out.");
        };

        private final AtomicReference<ResultSupplier<T>> resultCallable = new AtomicReference<>(unspecifiedResult);

        private final FutureTask<T> futureTask = new FutureTask<T>(() -> resultCallable.get().supply());

        @Override
        public boolean isCancelled() {
            return futureTask.isCancelled();
        }

        @Override
        public boolean isDone() {
            return futureTask.isDone();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return futureTask.cancel(mayInterruptIfRunning);
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return futureTask.get();
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return futureTask.get(timeout, unit);
        }

        @Override
        public void run() {
            futureTask.run();
        }

        /**
         * Sets the result of this {@link RemoteInvocationFutureTask}.
         *
         * @param result the result obbject
         * @return true if the set was successful
         */
        public boolean setResult(final T result) {
            logger.info("Setting remote result {}", result);
            return setResultCallable(() -> result);
        }

        /**
         * Sets the exception to this {@link RemoteInvocationFutureTask}.
         * '
         * @param exception the {@link Exception} to set
         */
        public boolean setException(final Exception exception) {

            logger.info("Setting remote exception {}", exception);

            return setResultCallable(() -> {
                throw exception;
            });

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
                run();
                return true;
            } else {
                return false;
            }
        }

    }

    @FunctionalInterface
    private interface ResultSupplier<T> {

        T supply() throws Exception;

    }

    private enum HandleResult {
        SYNC_RESULT,
        SYNC_ERROR,
        ASYNC_RESULT,
        ASYNC_ERROR
    }

}
