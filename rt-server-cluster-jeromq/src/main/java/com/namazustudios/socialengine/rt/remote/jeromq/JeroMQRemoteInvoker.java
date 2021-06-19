package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.AsyncConnectionPool;
import com.namazustudios.socialengine.rt.AsyncConnectionService;
import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQAsyncOperation.State.CONNECTION_ACQUIRED;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.zeromq.SocketType.DEALER;

public class JeroMQRemoteInvoker implements RemoteInvoker {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQRemoteInvoker.class);

    private String connectAddress;

    private PayloadReader payloadReader;

    private PayloadWriter payloadWriter;

    private AsyncConnectionService<ZContext, ZMQ.Socket> asyncConnectionService;

    private int minConnections;

    private int maxConnections;

    private final AtomicReference<AsyncConnectionPool<ZContext, ZMQ.Socket>> pool = new AtomicReference<>();

    @Override
    public void start(final String connectAddress, final long timeout, final TimeUnit timeoutTimeUnit) {

        final long timeoutMillis = MILLISECONDS.convert(timeout, timeoutTimeUnit);

        this.connectAddress = connectAddress;
        logger.info("Starting with connect address {} and timeout {}msec", connectAddress, timeoutMillis);

        final String name = JeroMQRemoteInvoker.class.getSimpleName() + ": " + connectAddress;

        final AsyncConnectionPool<ZContext, ZMQ.Socket> pool = getAsyncConnectionService().allocatePool(
            name, getMinConnections(), getMaxConnections(),
            zContext -> {
                final ZMQ.Socket socket = zContext.createSocket(DEALER);
                socket.connect(connectAddress);
                socket.setReceiveTimeOut((int)timeoutMillis);
                return socket;
            }
        );

        if (!this.pool.compareAndSet(null, pool)) {
            pool.close();
            throw new IllegalStateException("Already started.");
        }

    }

    @Override
    public void stop() {

        logger.info("Stopping connection to {}", connectAddress);
        connectAddress = null;

        final var pool = this.pool.getAndSet(null);
        if (pool == null) throw new IllegalStateException("Not running.");
        pool.close();

    }

    @Override
    public AsyncOperation invokeAsync(
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final var asyncOperation = new JeroMQAsyncOperation();

        final var mdcContext = MDC.getCopyOfContextMap();

        getPool().acquireNextAvailableConnection(connection -> {

            final var cs = asyncOperation.acquire(connection);

            if (CONNECTION_ACQUIRED.equals(cs.getState())) {

                // We proceed because the operation still indicates we should acquire the connection.

                final var jeroMQInvocation = new JeroMQRemoteInvocation(
                        asyncOperation,
                        connection,
                        invocation,
                        getPayloadReader(),
                        getPayloadWriter(),
                        mdcContext,
                        o -> { if (o != null) logger.warn("Async method returned value."); },
                        ex -> logger.warn("Async method threw exception.", ex),
                        asyncInvocationResultConsumerList,
                        asyncInvocationErrorConsumer
                );

                logger.debug("Sending {} asynchronously.", jeroMQInvocation);

            } else {

                // We put it back in the pool because we were requested a cancellation before the connection was ever
                // assigned to to this invocation.

                connection.recycle();
                asyncInvocationErrorConsumer.accept(cs.getInvocationError());
                logger.debug("Canceled {} before connection assignment. Recycling.", invocation);

            }

        });

        return asyncOperation;

    }

    @Override
    public CompletionStage<Object> invokeCompletionStage(
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final var ref = new AtomicReference<>();
        final var asyncOperation = new JeroMQAsyncOperation();

        final var mdcContext = MDC.getCopyOfContextMap();
        final var completableFuture = new CompletableFuture<>() {

            @Override
            public String toString() {
                return format("%s<Object> {%s}", CompletableFuture.class.getSimpleName(), ref.get());
            }

        }.exceptionally(throwable -> {

            if (throwable instanceof CancellationException) {
                asyncOperation.cancel();
            }

            return throwable;

        });

        getPool().acquireNextAvailableConnection(connection -> {

            ref.set(connection);

            final var cs = asyncOperation.acquire(connection);

            if (CONNECTION_ACQUIRED.equals(cs.getState())) {

                final var jeroMQInvocation = new JeroMQRemoteInvocation(
                        asyncOperation,
                        connection,
                        invocation,
                        getPayloadReader(),
                        getPayloadWriter(),
                        mdcContext,
                        completableFuture::complete,
                        completableFuture::completeExceptionally,
                        asyncInvocationResultConsumerList,
                        asyncInvocationErrorConsumer
                );

                logger.debug("Sending {} asynchronously.", jeroMQInvocation);

            } else {
                connection.recycle();
                logger.debug("Canceled {} before connection assignment. Recycling.", invocation);
                completableFuture.completeExceptionally(cs.getError());
            }

        });

        return completableFuture;

    }

    private AsyncConnectionPool<ZContext, ZMQ.Socket> getPool() {
        final var pool = this.pool.get();
        if (pool == null) throw new IllegalStateException("Not currently running.");
        return pool;
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

    public int getMinConnections() {
        return minConnections;
    }

    @Inject
    public void setMinConnections(@Named(REMOTE_INVOKER_MIN_CONNECTIONS) int minConnections) {
        this.minConnections = minConnections;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    @Inject
    public void setMaxConnections(@Named(REMOTE_INVOKER_MAX_CONNECTIONS) int maxConnections) {
        this.maxConnections = maxConnections;
    }

    @Override
    public String toString() {
        return "JeroMQRemoteInvoker{" +
                "connectAddress='" + connectAddress + '\'' +
                '}';
    }

}
