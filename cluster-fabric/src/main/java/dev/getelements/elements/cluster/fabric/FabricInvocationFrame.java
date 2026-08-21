package dev.getelements.elements.cluster.fabric;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * The wire framing used by {@link FabricEndpoint} and {@link JakartaWebsocketRemoteInvoker} for a single WebSocket binary
 * message: a one-byte {@link Type} tag followed by a payload already serialized via {@code PayloadReader}/
 * {@code PayloadWriter}.
 */
public final class FabricInvocationFrame {

    /**
     * The kind of payload carried by a frame.
     */
    public enum Type {

        /**
         * The payload is a serialized {@code Invocation}, sent client-to-server.
         */
        INVOCATION,

        /**
         * The payload is a serialized {@code InvocationResult}, sent server-to-client.
         */
        RESULT,

        /**
         * The payload is a serialized {@code InvocationError}, sent server-to-client.
         */
        ERROR

    }

    private final Type type;

    private final byte[] payload;

    public FabricInvocationFrame(final Type type, final byte[] payload) {
        this.type = type;
        this.payload = payload;
    }

    public Type getType() {
        return type;
    }

    public byte[] getPayload() {
        return payload;
    }

    /**
     * Encodes this frame as a single byte array suitable for a WebSocket binary message.
     */
    public byte[] toBytes() {
        final var buffer = ByteBuffer.allocate(1 + payload.length);
        buffer.put((byte) type.ordinal());
        buffer.put(payload);
        return buffer.array();
    }

    /**
     * Decodes a frame previously encoded by {@link #toBytes()}.
     */
    public static FabricInvocationFrame fromBytes(final byte[] bytes) {

        if (bytes.length < 1) {
            throw new IllegalArgumentException("Frame too short: " + bytes.length + " bytes.");
        }

        final var types = Type.values();
        final var ordinal = bytes[0];

        if (ordinal < 0 || ordinal >= types.length) {
            throw new IllegalArgumentException("Unknown frame type ordinal: " + ordinal);
        }

        final var payload = Arrays.copyOfRange(bytes, 1, bytes.length);
        return new FabricInvocationFrame(types[ordinal], payload);

    }

}
