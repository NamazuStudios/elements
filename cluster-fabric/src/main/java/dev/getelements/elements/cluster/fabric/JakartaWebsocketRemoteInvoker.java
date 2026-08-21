package dev.getelements.elements.cluster.fabric;

import dev.getelements.elements.rt.PayloadReader;
import dev.getelements.elements.rt.PayloadWriter;
import dev.getelements.elements.rt.remote.*;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static dev.getelements.elements.cluster.fabric.FabricInvocationFrame.Type.INVOCATION;

/**
 * The client side of the Fabric WebSocket transport (issue #10). Each invocation dials a fresh connection to the
 * peer's {@code /cluster/v1} endpoint, sends exactly one {@link Invocation} frame, receives exactly one response
 * frame, and closes &mdash; there is no persistent connection or connection pooling. Instances of this class are
 * minted per remote instance (one per {@link dev.getelements.elements.rt.remote.InstanceConnectionService.InstanceConnection}),
 * not shared globally, but a single instance is safe to use concurrently across many invocations since all
 * per-call state lives in a fresh {@link InvocationEndpoint} instance for each call.
 */
public class JakartaWebsocketRemoteInvoker implements RemoteInvoker {

    private static final Logger logger = LoggerFactory.getLogger(JakartaWebsocketRemoteInvoker.class);

    private static final ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        final var thread = new Thread(r, JakartaWebsocketRemoteInvoker.class.getSimpleName() + " timeout scheduler");
        thread.setDaemon(true);
        return thread;
    });

    private final PayloadReader payloadReader;

    private final PayloadWriter payloadWriter;

    private volatile String connectAddress;

    private volatile long timeout = DEFAULT_TIMEOUT;

    private volatile TimeUnit timeoutTimeUnit = DEFAULT_TIMEOUT_UNITS;

    @Inject
    public JakartaWebsocketRemoteInvoker(final PayloadReader payloadReader, final PayloadWriter payloadWriter) {
        this.payloadReader = payloadReader;
        this.payloadWriter = payloadWriter;
    }

    @Override
    public String getConnectAddress() {
        return connectAddress;
    }

    @Override
    public void start(final String connectAddress, final long timeout, final TimeUnit timeoutTimeUnit) {
        this.connectAddress = connectAddress;
        this.timeout = timeout;
        this.timeoutTimeUnit = timeoutTimeUnit;
    }

    @Override
    public AsyncOperation invokeAsync(final Invocation invocation,
                                       final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                       final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final var future = dial(invocation);

        future.whenComplete((invocationResult, throwable) -> {
            if (throwable == null) {
                asyncInvocationResultConsumerList.forEach(c -> c.accept(invocationResult));
            } else {
                asyncInvocationErrorConsumer.accept(toInvocationError(throwable));
            }
        });

        return new AsyncOperation() {

            @Override
            public void cancel() {
                future.cancel(true);
            }

            @Override
            public void timeout(final long time, final TimeUnit timeUnit) {
                timeoutScheduler.schedule(() -> future.cancel(true), time, timeUnit);
            }

        };

    }

    @Override
    public CompletionStage<Object> invokeCompletionStage(final Invocation invocation,
                                                          final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                                          final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final var resultFuture = new CompletableFuture<Object>();

        dial(invocation).whenComplete((invocationResult, throwable) -> {
            if (throwable == null) {
                asyncInvocationResultConsumerList.forEach(c -> c.accept(invocationResult));
                resultFuture.complete(invocationResult.getResult());
            } else {
                asyncInvocationErrorConsumer.accept(toInvocationError(throwable));
                resultFuture.completeExceptionally(throwable);
            }
        });

        return resultFuture;

    }

    private static InvocationError toInvocationError(final Throwable throwable) {
        final var invocationError = new InvocationError();
        invocationError.setThrowable(throwable);
        return invocationError;
    }

    private CompletableFuture<InvocationResult> dial(final Invocation invocation) {

        final var endpoint = new InvocationEndpoint(payloadReader, payloadWriter, invocation);
        final var scheduledTimeout = timeoutScheduler.schedule(
                () -> endpoint.future.completeExceptionally(new java.util.concurrent.TimeoutException(
                        "Timed out after " + timeout + " " + timeoutTimeUnit + " connecting to " + connectAddress)),
                timeout, timeoutTimeUnit);

        endpoint.future.whenComplete((r, t) -> scheduledTimeout.cancel(false));

        try {
            final var container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(endpoint, URI.create(connectAddress));
        } catch (final Exception ex) {
            endpoint.future.completeExceptionally(ex);
        }

        return endpoint.future;

    }

    @ClientEndpoint
    public static class InvocationEndpoint {

        private final PayloadReader payloadReader;

        private final PayloadWriter payloadWriter;

        private final Invocation invocation;

        private final CompletableFuture<InvocationResult> future = new CompletableFuture<>();

        public InvocationEndpoint(final PayloadReader payloadReader,
                                  final PayloadWriter payloadWriter,
                                  final Invocation invocation) {
            this.payloadReader = payloadReader;
            this.payloadWriter = payloadWriter;
            this.invocation = invocation;
        }

        @OnOpen
        public void onOpen(final Session session) {
            try {
                final var payload = payloadWriter.write(invocation);
                final var frame = new FabricInvocationFrame(INVOCATION, payload);
                session.getBasicRemote().sendBinary(ByteBuffer.wrap(frame.toBytes()));
            } catch (final IOException ex) {
                future.completeExceptionally(ex);
            }
        }

        @OnMessage
        public void onMessage(final Session session, final ByteBuffer message) {
            try {

                final var bytes = new byte[message.remaining()];
                message.get(bytes);
                final var frame = FabricInvocationFrame.fromBytes(bytes);

                switch (frame.getType()) {
                    case RESULT -> future.complete(payloadReader.read(InvocationResult.class, frame.getPayload()));
                    case ERROR -> {
                        final var error = payloadReader.read(InvocationError.class, frame.getPayload());
                        future.completeExceptionally(error.getThrowable());
                    }
                    default -> future.completeExceptionally(
                            new IllegalStateException("Unexpected Fabric frame type: " + frame.getType()));
                }

            } catch (final Exception ex) {
                future.completeExceptionally(ex);
            } finally {
                try {
                    session.close();
                } catch (final IOException ex) {
                    logger.debug("Failed to close session cleanly.", ex);
                }
            }
        }

        @OnError
        public void onError(final Session session, final Throwable thr) {
            future.completeExceptionally(thr);
        }

    }

}
