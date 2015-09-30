package com.namazustudios.socialengine.rt.protocol;

import javolution.io.Struct;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A type that is used to encapsulate a specific payload.  This is a header for the encapsulated data.
 *
 * Created by patricktwohig on 9/29/15.
 */
public class Envelope extends Struct {

    private static final Envelope PROTOTYPE = new Envelope();

    private final Signed8 envelopeType = new Signed8();

    private final Signed8 envelopeFormat = new Signed8();

    private final Unsigned32 checksum = new Unsigned32();

    private final Unsigned32 length = new Unsigned32();

    /**
     * Gets the total size of the envelope header.
     *
     * @return the size of the envelope header
     */
    public static final int envelopeHeaderSize() {
        return PROTOTYPE.size();
    }

    /**
     * Returns the {@link EnvelopeType}
     * @return the type of the envelope
     *
     * @throws {@link IllegalArgumentException} if the type is not valid
     */
    public EnvelopeType getEnvelopeType() {
        return EnvelopeType.get(envelopeType.get());
    }

    /**
     * Sets the {@link EnvelopeType} as an 8-bit number.
     *
     * @throws {@link IllegalArgumentException} if the type is not valid
     */
    public void setEnvelopeType(final EnvelopeType envelopeType) {
        this.envelopeType.set(envelopeType.getCode());
    }

    /**
     * Gets the envelope format.
     *
     * @return the envelope format
     * @throws {@link IllegalArgumentException} if the type is not valid
     */
    public EnvelopeFormat getEnvelopeFormat() {
        return EnvelopeFormat.get(envelopeFormat.get());
    }

    /**
     * Returns the {@link EnvelopeFormat}
     * @return the format of the envelope
     *
     * @throws {@link IllegalArgumentException} if the type is not valid
     */
    public void setEnvelopeFormat(final EnvelopeFormat envelopeFormat) {
        this.envelopeFormat.set(envelopeFormat.getCode());
    }

    /**
     * Returns the checksum of the envelope and the following payload.
     * @return the checksum of the envelope
     */
    public long getChecksum() {
        return checksum.get();
    }

    /**
     * Sets the checksum of the envelope and the following payload.
     */
    public void setChecksum(final long checksum) {
        this.checksum.set(checksum);
    }

    /**
     * Gets the length of the envelope.
     * @return the length of the envelope
     */
    public long getLength() {
        return this.length.get();
    }

    /**
     * Sets the length of the envelope.
     *
     * @param length the length
     */
    public void setLength(final long length) {
        this.length.set(length);
    }

    @Override
    public final boolean isPacked() {
        return true;
    }

    @Override
    public final ByteOrder byteOrder() {
        return ByteOrder.BIG_ENDIAN;
    }

    /**
     * Used to build {@link Envelope} instances.
     */
    public static class Builder {

        private EnvelopeType envelopeType;

        private EnvelopeFormat envelopeFormat;

        /**
         * Specifies the type of the envelope.
         *
         * @param envelopeType the envelope type
         * @return this builder instance
         */
        public Builder type(final EnvelopeType envelopeType) {
            this.envelopeType = envelopeType;
            return this;
        }

        /**
         * Specifies the type of the envelope.
         *
         * @param envelopeFormat the envelope format
         * @return this builder instance
         */
        public Builder format(final EnvelopeFormat envelopeFormat) {
            this.envelopeFormat = envelopeFormat;
            return this;
        }

        /**
         * Returns the {@link Envelope} instance with the given backing {@link ByteBuffer}.  The buffer's position
         * will be advanced by the size of this and the values written to the buffer.
         *
         * The length wil be set to the remaining bytes in the buffer.
         *
         * @return the {@link Envelope} instance
         */
        public Envelope build(final ByteBuffer byteBuffer) {

            if (envelopeType == null) {
                throw new IllegalStateException("envelope type not specified.");
            }

            if (envelopeFormat == null) {
                throw new IllegalStateException("envelope format not specified.");
            }

            final Envelope envelope = new Envelope();
            envelope.setByteBuffer(byteBuffer, byteBuffer.position());
            byteBuffer.position(byteBuffer.position() + envelope.size());

            envelope.setEnvelopeType(envelopeType);
            envelope.setEnvelopeFormat(envelopeFormat);
            envelope.setLength(byteBuffer.remaining());

            return envelope;

        }

    }

}
