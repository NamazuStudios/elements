package dev.getelements.elements.cluster.fabric;

import dev.getelements.elements.rt.PayloadReader;
import dev.getelements.elements.rt.PayloadWriter;
import dev.getelements.elements.rt.remote.Invocation;
import dev.getelements.elements.rt.remote.InvocationError;
import dev.getelements.elements.rt.remote.InvocationResult;
import jakarta.inject.Inject;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static dev.getelements.elements.cluster.fabric.FabricInvocationFrame.Type.INVOCATION;

/**
 * The client side of the Fabric WebSocket transport prototype (issue #10). Dials a fresh connection to a peer's
 * {@code /cluster/v1} endpoint, sends a single {@link Invocation}, and blocks for the one expected response frame.
 *
 * <p>No connection pooling, retry, or reconnect logic. Those are later phases, once this round-trip is proven.</p>
 */
public class FabricRemoteInvoker {

    private static final long DEFAULT_TIMEOUT_SECONDS = 30;

    private final PayloadReader payloadReader;

    private final PayloadWriter payloadWriter;

    @Inject
    public FabricRemoteInvoker(final PayloadReader payloadReader, final PayloadWriter payloadWriter) {
        this.payloadReader = payloadReader;
        this.payloadWriter = payloadWriter;
    }

    /**
     * Opens a single connection to {@code uri}, sends {@code invocation}, and blocks for the result.
     *
     * @param uri the {@code ws://} or {@code wss://} URI of the peer's Fabric endpoint
     * @param invocation the invocation to send
     * @return the {@link InvocationResult}
     * @throws Exception if the connection fails, times out, or the remote side reports an {@link InvocationError}
     */
    public InvocationResult invoke(final URI uri, final Invocation invocation) throws Exception {

        final var future = new CompletableFuture<InvocationResult>();
        final var container = ContainerProvider.getWebSocketContainer();
        final var config = ClientEndpointConfig.Builder.create().build();

        final var endpoint = new jakarta.websocket.Endpoint() {

            @Override
            public void onOpen(final Session session, final EndpointConfig endpointConfig) {

                session.addMessageHandler(new MessageHandler.Whole<ByteBuffer>() {
                    @Override
                    public void onMessage(final ByteBuffer message) {
                        onFrame(message, future);
                    }
                });

                try {
                    final var payload = payloadWriter.write(invocation);
                    final var frame = new FabricInvocationFrame(INVOCATION, payload);
                    session.getBasicRemote().sendBinary(ByteBuffer.wrap(frame.toBytes()));
                } catch (final IOException ex) {
                    future.completeExceptionally(ex);
                }

            }

            @Override
            public void onError(final Session session, final Throwable thr) {
                future.completeExceptionally(thr);
            }

        };

        try (final var session = container.connectToServer(endpoint, config, uri)) {
            return future.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }

    }

    private void onFrame(final ByteBuffer message, final CompletableFuture<InvocationResult> future) {
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
        }
    }

}
