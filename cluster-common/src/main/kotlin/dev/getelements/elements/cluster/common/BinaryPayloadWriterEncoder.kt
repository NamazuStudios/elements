package dev.getelements.elements.cluster.common

import dev.getelements.elements.cluster.common.Constants.ENCODER_MAGIC_HEADER
import dev.getelements.elements.cluster.common.dto.Envelope
import dev.getelements.elements.sdk.util.io.PayloadWriter
import jakarta.websocket.Encoder
import jakarta.websocket.EndpointConfig
import java.nio.ByteBuffer

/**
 * Jakarta WebSocket binary [Encoder] that serializes an outgoing [Envelope] into a wire frame consisting of
 * a magic header, the envelope's [Envelope.type] ordinal, and the payload bytes produced by a [PayloadWriter].
 * Registered on client/server endpoint configs alongside [BinaryPayloadReaderDecoder], its counterpart.
 */
class BinaryPayloadWriterEncoder : Encoder.Binary<Envelope<Any>> {

    companion object {

        /**
         * Key under which the [PayloadWriter] to use must be supplied via
         * [jakarta.websocket.EndpointConfig.getUserProperties], since the WebSocket container instantiates
         * encoders itself via a no-arg constructor rather than allowing constructor injection.
         */
        val PAYLOAD_WRITER = "dev.getelements.elements.cluster.common.paylaod.writer"
    }

    lateinit var payloadWriter: PayloadWriter

    override fun init(config: EndpointConfig) {
        payloadWriter = config.userProperties[PAYLOAD_WRITER]
            as? PayloadWriter
            ?: throw IllegalStateException("Payload writer is not set in encoder properties.")
    }

    override fun encode(`object`: Envelope<Any>?): ByteBuffer? {

        val payload = payloadWriter.write(`object`)
        val buffer = ByteBuffer.allocateDirect(Int.SIZE_BYTES * 2 + payload.size)

        return buffer
            .putInt(ENCODER_MAGIC_HEADER)
            .putInt(`object`?.type?.ordinal ?: Envelope.Type.NULL.ordinal)
            .put(payload, 0, payload.size)
            .flip()

    }

}
