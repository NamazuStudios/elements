package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.jeromq.ConnectionPool;
import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZPoller;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.lang.Thread.interrupted;

public class JeroMQRemoteInvoker implements RemoteInvoker {

    public static final String NODE_ADDRESS = "com.namazustudios.socialengine.remote.jeromq.JeroMQRemoteInvoker.nodeAddress";

    private static final Logger logger = LoggerFactory.getLogger(JeroMQRemoteInvoker.class);

    private final Set<LatchedFuture<Object>> futureSet = new ConcurrentHashMap<LatchedFuture<Object>, Object>().keySet();

    private String nodeAddress;

    private MessageWriter messageWriter;

    private MessageReader messageReader;

    private ConnectionPool connectionPool;

    @Override
    public void start() {
        getConnectionPool().start(zContext -> {
            final ZMQ.Socket socket = zContext.createSocket(ZMQ.DEALER);
            socket.connect(getNodeAddress());
            return socket;
        });
    }

    @Override
    public void stop() {
        getConnectionPool().stop();
        futureSet.forEach(futureSet -> futureSet.cancel(true));
        futureSet.clear();
    }

    @Override
    public Future<Object> invoke(final Invocation invocation,
                                 final Consumer<InvocationError> invocationErrorConsumer,
                                 final List<Consumer<InvocationResult>> invocationResultConsumerList) {

        final LatchedFuture<Object> latchedFuture = new LatchedFuture<>();

        getConnectionPool().process(connection -> {

            try (final ZPoller poller = new ZPoller(connection.context())) {

                send(connection.socket(), invocation, invocationResultConsumerList.size());
                poller.register(connection.socket(), ZPoller.READABLE);

                final int expectedResponseCount = 1 + invocationResultConsumerList.size();

                for (int received = 0; received < expectedResponseCount && !interrupted(); ++received) {

                    poller.poll(-1);

                    final ResponseHeader responseHeader = receiveHeader(connection.socket());

                    final boolean success = handleResponse(connection.socket(),
                                                           responseHeader,
                                                           invocationErrorConsumer,
                                                           result -> latchedFuture.setResultCallable(() -> result),
                                                           invocationResultConsumerList);

                    if (!success) {
                        break;
                    }

                }

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

            }

        });

        futureSet.add(latchedFuture);
        return latchedFuture;

    }

    private void send(final ZMQ.Socket socket, final Invocation invocation, final int additionalCount) {

        final RequestHeader requestHeader = new RequestHeader();
        requestHeader.additionalParts.set(additionalCount);

        final byte[] payload = getMessageWriter().write(invocation);

        socket.sendByteBuffer(requestHeader.getByteBuffer(), ZMQ.SNDMORE);
        socket.send(payload);

    }

    private boolean handleResponse(final ZMQ.Socket socket,
                                   final ResponseHeader responseHeader,
                                   final Consumer<InvocationError> invocationErrorConsumer,
                                   final Consumer<InvocationResult> resultObjectCallable,
                                   final List<Consumer<InvocationResult>> invocationResultConsumerList) {

        final int part = responseHeader.part.get();

        switch (responseHeader.type.get()) {
            case INVOCATION_ERROR:
                handleError(socket, invocationErrorConsumer);
                return false;
            case INVOCATION_RESULT:
                handleResult(socket, part == 0 ? resultObjectCallable : invocationResultConsumerList.get(part - 1));
                return true;
            default:
                logger.error("Invalid response type {}", responseHeader.type.get());
                throw new InternalError("Invalid response type " + responseHeader.type.get());
        }

    }

    private void handleError(final ZMQ.Socket socket, final Consumer<InvocationError> invocationErrorConsumer) {
        final byte[] bytes = socket.recv();
        final InvocationError invocationError = getMessageReader().read(bytes, InvocationError.class);
        invocationErrorConsumer.accept(invocationError);
    }

    private void handleResult(final ZMQ.Socket socket, final Consumer<InvocationResult> invocationResultConsumer) {
        final byte[] bytes = socket.recv();
        final InvocationResult invocationResult = getMessageReader().read(bytes, InvocationResult.class);
        invocationResultConsumer.accept(invocationResult);
    }

    private ResponseHeader receiveHeader(final ZMQ.Socket socket) {

        final byte[] bytes = socket.recv();
        final ResponseHeader responseHeader = new ResponseHeader();

        responseHeader.getByteBuffer().flip();
        responseHeader.getByteBuffer().put(bytes);

        return responseHeader;
    }

    public MessageWriter getMessageWriter() {
        return messageWriter;
    }

    @Inject
    public void setMessageWriter(MessageWriter messageWriter) {
        this.messageWriter = messageWriter;
    }

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    @Inject
    public void setConnectionPool(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public MessageReader getMessageReader() {
        return messageReader;
    }

    @Inject
    public void setMessageReader(MessageReader messageReader) {
        this.messageReader = messageReader;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    @Inject
    public void setNodeAddress(@Named(NODE_ADDRESS) String nodeAddress) {
        this.nodeAddress = nodeAddress;
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

        private final Callable<T> unspecifiedResult = () -> {
            throw new InternalException("Interrupted or request timed out.");
        };

        private final AtomicReference<Callable<T>> resultCallable = new AtomicReference<>(unspecifiedResult);

        private final CountDownLatch latch = new CountDownLatch(1);

        private final FutureTask<T> futureTask = new FutureTask<T>(() -> {
            latch.await();
            return resultCallable.get().call();
        });

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return futureTask.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return futureTask.isCancelled();
        }

        @Override
        public boolean isDone() {
            return futureTask.isDone();
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return futureTask.get();
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return futureTask.get(timeout, unit);
        }

        /**
         * Sets the result {@link Callable<T>} and releases the latch allowing the supplied result to be available
         * immediately after this call returns.  If a result has previously been set, then this simply rejects the
         * changed value and does nothing.
         *
         *
         * @param resultCallable the result {@link Callable<T>}
         * @return true if the result was set, or false if a previous result was alredy set.
         */
        public boolean setResultCallable(final Callable<T> resultCallable) {
            if (this.resultCallable.compareAndSet(unspecifiedResult, resultCallable)) {
                futureSet.remove(this);
                latch.countDown();
                return true;
            } else {
                return false;
            }
        }

    }

}
