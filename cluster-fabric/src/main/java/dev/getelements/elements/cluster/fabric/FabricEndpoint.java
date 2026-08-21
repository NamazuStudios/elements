package dev.getelements.elements.cluster.fabric;

import dev.getelements.elements.rt.PayloadReader;
import dev.getelements.elements.rt.PayloadWriter;
import dev.getelements.elements.rt.ResultHandlerStrategy;
import dev.getelements.elements.rt.jrpc.SingleSyncReturnResultHandlerStrategy;
import dev.getelements.elements.rt.remote.Invocation;
import dev.getelements.elements.rt.remote.InvocationError;
import dev.getelements.elements.rt.remote.InvocationResult;
import dev.getelements.elements.rt.remote.LocalInvocationDispatcher;
import jakarta.inject.Inject;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

import static dev.getelements.elements.cluster.fabric.FabricInvocationFrame.Type.ERROR;
import static dev.getelements.elements.cluster.fabric.FabricInvocationFrame.Type.RESULT;

/**
 * The server side of the Fabric WebSocket transport prototype (issue #10). Accepts a single serialized
 * {@link Invocation} per binary message and dispatches it via the existing (deprecated)
 * {@link LocalInvocationDispatcher} mechanism, mirroring how {@code JsonRpcResource} dispatches over HTTP.
 *
 * <p>This is a transport-only prototype: one invocation per message, no multiplexing, no auth, no reconnect
 * handling. Those are later phases, once this round-trip is proven.</p>
 */
public class FabricEndpoint extends jakarta.websocket.Endpoint {

    /**
     * The fixed path of the Fabric endpoint, identical on every instance.
     */
    public static final String CLUSTER_WS_PATH = "/cluster/v1";

    private static final Logger logger = LoggerFactory.getLogger(FabricEndpoint.class);

    private final LocalInvocationDispatcher localInvocationDispatcher;

    private final PayloadReader payloadReader;

    private final PayloadWriter payloadWriter;

    @Inject
    public FabricEndpoint(final LocalInvocationDispatcher localInvocationDispatcher,
                           final PayloadReader payloadReader,
                           final PayloadWriter payloadWriter) {
        this.localInvocationDispatcher = localInvocationDispatcher;
        this.payloadReader = payloadReader;
        this.payloadWriter = payloadWriter;
    }

    @Override
    public void onOpen(final Session session, final EndpointConfig config) {
        session.addMessageHandler(new MessageHandler.Whole<ByteBuffer>() {
            @Override
            public void onMessage(final ByteBuffer message) {
                handle(session, message);
            }
        });
    }

    @Override
    public void onError(final Session session, final Throwable thr) {
        logger.error("Fabric WebSocket session {} error.", session.getId(), thr);
    }

    private void handle(final Session session, final ByteBuffer message) {

        final var bytes = new byte[message.remaining()];
        message.get(bytes);

        final Invocation invocation;

        try {
            final var frame = FabricInvocationFrame.fromBytes(bytes);
            invocation = payloadReader.read(Invocation.class, frame.getPayload());
        } catch (final Exception ex) {
            logger.error("Failed to decode Fabric invocation frame.", ex);
            return;
        }

        final ResultHandlerStrategy resultHandlerStrategy = new SingleSyncReturnResultHandlerStrategy();

        resultHandlerStrategy.onFinalResult(result -> send(session, RESULT, new InvocationResult(result)));

        resultHandlerStrategy.onError(throwable -> {
            final var invocationError = new InvocationError();
            invocationError.setThrowable(throwable);
            send(session, ERROR, invocationError);
        });

        localInvocationDispatcher.dispatch(invocation, resultHandlerStrategy);

    }

    private void send(final Session session, final FabricInvocationFrame.Type type, final Object payload) {
        try {
            final var frame = new FabricInvocationFrame(type, payloadWriter.write(payload));
            session.getBasicRemote().sendBinary(ByteBuffer.wrap(frame.toBytes()));
        } catch (final IOException ex) {
            logger.error("Failed to send Fabric invocation frame.", ex);
        }
    }

}
