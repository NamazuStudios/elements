package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.jeromq.AsyncConnectionPool;
import com.namazustudios.socialengine.rt.remote.Invocation;
import com.namazustudios.socialengine.rt.remote.InvocationErrorConsumer;
import com.namazustudios.socialengine.rt.remote.InvocationResult;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.zeromq.ZMQ;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.zeromq.SocketType.DEALER;

public class JeroMQRemoteInvoker implements RemoteInvoker {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQRemoteInvoker.class);

    private String connectAddress;

    private PayloadReader payloadReader;

    private PayloadWriter payloadWriter;

    private AsyncConnectionPool asyncConnectionPool;

    @Override
    public void start(final String connectAddress, final long timeout, final TimeUnit timeoutTimeUnit) {

        final long timeoutMillis = MILLISECONDS.convert(timeout, timeoutTimeUnit);

        this.connectAddress = connectAddress;
        logger.info("Starting with connect address {} and timeout {}msec", connectAddress, timeoutMillis);

        getAsyncConnectionPool().start(zContext -> {
            final ZMQ.Socket socket = zContext.createSocket(DEALER);
            socket.connect(connectAddress);
            socket.setReceiveTimeOut((int)timeoutMillis);
            return socket;
        }, JeroMQRemoteInvoker.class.getSimpleName() + ": " + connectAddress);

    }

    @Override
    public void stop() {
        getAsyncConnectionPool().stop();
        logger.info("Stopping connection to {}", connectAddress);
        connectAddress = null;
    }

    @Override
    public Void invokeAsync(
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final Map<String, String > mdcContext = MDC.getCopyOfContextMap();

        getAsyncConnectionPool().acquireNextAvailableConnection(connection -> {

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
            jeroMQInvocation.send();

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

        getAsyncConnectionPool().acquireNextAvailableConnection(connection -> {

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
            jeroMQInvocation.send();

        });

        return completableFuture;

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

    public AsyncConnectionPool getAsyncConnectionPool() {
        return asyncConnectionPool;
    }

    @Inject
    public void setAsyncConnectionPool(AsyncConnectionPool asyncConnectionPool) {
        this.asyncConnectionPool = asyncConnectionPool;
    }

}
