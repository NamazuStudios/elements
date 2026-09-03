package dev.getelements.elements.cluster.common

import dev.getelements.elements.cluster.common.dto.Envelope
import dev.getelements.elements.cluster.common.dto.Envelope.Type.INSTANCE_METADATA
import dev.getelements.elements.cluster.common.dto.Envelope.Type.INVOCATION
import dev.getelements.elements.cluster.common.dto.Envelope.Type.INVOCATION_ERROR
import dev.getelements.elements.cluster.common.dto.Envelope.Type.INVOCATION_RESULT
import dev.getelements.elements.cluster.common.dto.Envelope.Type.NULL
import dev.getelements.elements.cluster.common.dto.InstanceMetadataEnvelope
import dev.getelements.elements.cluster.common.dto.InvocationEnvelope
import dev.getelements.elements.cluster.common.dto.InvocationErrorEnvelope
import dev.getelements.elements.cluster.common.dto.InvocationResultEnvelope
import dev.getelements.elements.sdk.util.io.PayloadReader
import jakarta.websocket.Decoder
import jakarta.websocket.EndpointConfig
import java.nio.ByteBuffer

/**
 * Jakarta WebSocket binary [Decoder] that deserializes an incoming wire frame back into an [Envelope],
 * the counterpart to [BinaryPayloadWriterEncoder]. It validates the frame's magic header, reads the
 * [Envelope.Type] discriminator to determine the concrete envelope subtype, then delegates to a
 * [PayloadReader] to reconstruct it.
 */
class BinaryPayloadReaderDecoder : Decoder.Binary<Envelope<Any>> {

    companion object {

        /**
         * Key under which the [PayloadReader] to use must be supplied via
         * [jakarta.websocket.EndpointConfig.getUserProperties], since the WebSocket container instantiates
         * decoders itself via a no-arg constructor rather than allowing constructor injection.
         */
        val PAYLOAD_READER = "dev.getelements.elements.cluster.common.paylaod.reader"
    }

    lateinit var payloadReader: PayloadReader

    override fun init(config: EndpointConfig) {
        payloadReader = config.userProperties[PAYLOAD_READER]
            as? PayloadReader
            ?: throw IllegalStateException("Payload writer is not set in encoder properties.")
    }

    override fun decode(bytes: ByteBuffer?): Envelope<Any>? {

        val magic = bytes?.getInt()

        if (magic != Constants.ENCODER_MAGIC_HEADER)
            throw IllegalArgumentException("Magic header mismatch.")

        val ordinal = bytes.getInt(0)
        val envelopeType = Envelope.Type.entries[ordinal]

        return when (envelopeType) {
            NULL -> null
            INVOCATION -> payloadReader.read(InvocationEnvelope::class.java, bytes)
            INVOCATION_ERROR -> payloadReader.read(InvocationErrorEnvelope::class.java, bytes)
            INVOCATION_RESULT -> payloadReader.read(InvocationResultEnvelope::class.java, bytes)
            INSTANCE_METADATA -> payloadReader.read(InstanceMetadataEnvelope::class.java, bytes)
            else -> throw IllegalArgumentException("Unknown envelope type $envelopeType")
        }

    }

    override fun willDecode(bytes: ByteBuffer?): Boolean {
        return true
    }

}
