package dev.getelements.elements.cluster.common

/**
 * Shared constants for the cluster wire protocol, used by both [BinaryPayloadWriterEncoder] and
 * [BinaryPayloadReaderDecoder] so the two stay in sync on frame layout.
 */
object Constants {

    /**
     * Magic number written at the start of every encoded [dev.getelements.elements.cluster.common.dto.Envelope]
     * frame by [BinaryPayloadWriterEncoder] and verified by [BinaryPayloadReaderDecoder] before decoding,
     * to catch malformed or incompatible frames early.
     */
    const val ENCODER_MAGIC_HEADER = 0x00010000

}