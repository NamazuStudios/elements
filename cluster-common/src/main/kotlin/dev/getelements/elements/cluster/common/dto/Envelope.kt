package dev.getelements.elements.cluster.common.dto

/**
 * The wire-level message wrapping a single cluster protocol payload sent or received over the WebSocket
 * transport. Every message exchanged between cluster peers is framed as an [Envelope]: [type] identifies
 * which concrete subtype ([InvocationEnvelope], [InvocationResultEnvelope], [InvocationErrorEnvelope], or
 * [InstanceMetadataEnvelope]) the message is, which [BinaryPayloadWriterEncoder]/[BinaryPayloadReaderDecoder]
 * use as a discriminator to encode/decode the appropriate [payload] without relying on reflection.
 */
sealed interface Envelope<out PayloadT> {

    /**
     * The discriminator identifying the concrete [Envelope] subtype, and by extension the wire format of
     * [payload]. Used by [BinaryPayloadWriterEncoder] and [BinaryPayloadReaderDecoder] to encode/decode the
     * correct concrete type.
     */
    val type: Type

    /**
     * The actual protocol payload carried by this envelope.
     */
    val payload: PayloadT

    /**
     * Enumerates the kinds of [Envelope] that can appear on the wire.
     */
    enum class Type {

        /**
         * Indicates a null envelope; no payload is present.
         */
        NULL,

        /**
         * Indicates an [InvocationEnvelope] carrying a remote method invocation request.
         */
        INVOCATION,

        /**
         * Indicates an [InvocationResultEnvelope] carrying the successful result of a remote invocation.
         */
        INVOCATION_RESULT,

        /**
         * Indicates an [InvocationErrorEnvelope] carrying the failure of a remote invocation.
         */
        INVOCATION_ERROR,

        /**
         * Indicates an [InstanceMetadataEnvelope] carrying metadata describing a cluster instance.
         */
        INSTANCE_METADATA

    }

}
