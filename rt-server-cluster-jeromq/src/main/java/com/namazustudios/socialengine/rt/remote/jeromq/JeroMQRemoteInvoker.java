package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.AsyncConnectionPool;
import com.namazustudios.socialengine.rt.AsyncConnectionService;
import com.namazustudios.socialengine.rt.remote.Invocation;
import com.namazustudios.socialengine.rt.remote.InvocationErrorConsumer;
import com.namazustudios.socialengine.rt.remote.InvocationResult;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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

    private final AtomicReference<AsyncConnectionPool> pool = new AtomicReference<>();

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
        });

        if (!this.pool.compareAndSet(null, pool)) {
            pool.close();
            throw new IllegalStateException("Already started.");
        }

    }

    @Override
    public void stop() {

        logger.info("Stopping connection to {}", connectAddress);
        connectAddress = null;

        final AsyncConnectionPool pool = this.pool.getAndSet(null);
        if (pool == null) throw new IllegalStateException("Not running.");
        pool.close();

    }

    @Override
    public Void invokeAsync(
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final Map<String, String > mdcContext = MDC.getCopyOfContextMap();

        getPool().acquireNextAvailableConnection(connection -> {

            final JeroMQInvocation jeroMQInvocation = new JeroMQInvocation(
                connection,
                invocation,
                getPayloadReader(),
                getPayloadWriter(),
                mdcContext,
                o -> logger.warn("Async method returned value."),
                ex -> logger.warn("Async method threw exception.", ex),
                asyncInvocationResultConsumerList,
                asyncInvocationErrorConsumer
            );

            logger.debug("Sending {} asynchronously.", jeroMQInvocation);

        });

        return null;
    }

    @Override
    public CompletionStage<Object> invokeCompletionStage(
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final Map<String, String > mdcContext = MDC.getCopyOfContextMap();
        final CompletableFuture<Object> completableFuture = new CompletableFuture<>();

        getPool().acquireNextAvailableConnection(connection -> {

            final JeroMQInvocation jeroMQInvocation = new JeroMQInvocation(
                connection,
                invocation,
                getPayloadReader(),
                getPayloadWriter(),
                mdcContext,
                o -> completableFuture.complete(o),
                ex -> completableFuture.completeExceptionally(ex),
                asyncInvocationResultConsumerList,
                asyncInvocationErrorConsumer
            );

            logger.debug("Sending {} asynchronously.", jeroMQInvocation);

        });

        return completableFuture;

    }

    private AsyncConnectionPool<ZContext, ZMQ.Socket> getPool() {
        final AsyncConnectionPool pool = this.pool.get();
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

    @Override
    public String toString() {
        return "JeroMQRemoteInvoker{" +
                "connectAddress='" + connectAddress + '\'' +
                '}';
    }

}
