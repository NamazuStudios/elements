package dev.getelements.elements.rt.transact.unix;

import javolution.io.Struct;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

import static java.lang.Math.min;

/**
 * Which checksum algorithm to use when committing the transaction.
 */
public enum UnixFSChecksumAlgorithm {

    /**
     * Uses {@link CRC32}
     */
    CRC_32 {

        protected Algorithm newAlgorithm() {

            final CRC32 crc32 = new CRC32();

            return new Algorithm() {
                @Override
                public void update(final byte b) {
                    crc32.update(b);
                }

                @Override
                public void update(final ByteBuffer byteBuffer) {
                    crc32.update(byteBuffer);
                }

                @Override
                public long getValue() {
                    return crc32.getValue();
                }
            };

        }

    },

    /**
     * Uses {@link Adler32}
     */
    ADLER_32 {

        protected Algorithm newAlgorithm() {

            final Adler32 adler32 = new Adler32();

            return new Algorithm() {
                @Override
                public void update(final byte b) {
                    adler32.update(b);
                }

                @Override
                public void update(final ByteBuffer byteBuffer) {
                    adler32.update(byteBuffer);
                }

                @Override
                public long getValue() {
                    return adler32.getValue();
                }
            };

        }

    };

    /**
     * Checks if this the supplied {@link Checkable} is valid.
     *
     * @param checkable
     * @return true if valid, false otherwise
     */
    public boolean isValid(final Checkable checkable) {

        final var algorithm = newAlgorithm();

        final var contents = checkable.contentsToCheck();
        final var checksum = checkable.checksum();
        final long existing = checksum.get();

        // Original limit/position values of the buffer
        final int limit = contents.limit();
        final int position = contents.position();

        // Sets to read up to the beginning of the checksum member and updates the checksum.
        contents.limit(position + checksum.offset());
        algorithm.update(contents);

        // Inserts four zero bytes as if it were calculated with zeros in that position
        for (int i = 0; i < Integer.BYTES; ++i) algorithm.update((byte) 0x0);

        // Sets the limit and position to the remainder of the buffer and updates the checksum.
        contents.limit(limit).position(position + checksum.offset() + Integer.BYTES);
        algorithm.update(contents);

        final long calculated = algorithm.getValue();
        return existing == calculated;

    }

    /**
     * Checks if this the supplied {@link ChannelCheckable} is valid.
     *
     * @param checkable
     * @return true if valid, false otherwise
     */
    public boolean isValid(final long value, final ChannelCheckable checkable) throws IOException {
        return compute(checkable) == value;
    }

    protected abstract Algorithm newAlgorithm();

    /**
     * Computes the checksum and then sets the {@link Checkable#checksum()} value. This will compute the checksum for
     * the {@link Checkable} from the current bytebuffer position the buffer's current limit. It will then write the
     * value to the struct member and to the bytebuffer.
     *
     * @param checkable the structure to compute
     */
    public void compute(final Checkable checkable) {

        final var algo = newAlgorithm();

        final var checksum = checkable.checksum();
        final var contents = checkable.contentsToCheck();

        checksum.set(0);
        algo.update(contents);

        final long value = algo.getValue();
        checksum.set(value);

    }

    /**
     * Computes the checksum and then returns the value. This checks the entire contents of the channel from the current
     * position to the ending position.
     *
     * @param checkable the structure to compute
     */
    public long compute(final ChannelCheckable checkable) throws IOException {

        final var buffer = checkable.getIntermediateBuffer();
        final var contents = checkable.contentsToCheck();
        final var algorithm = newAlgorithm();

        // Calculates the rest of the checksum
        while (contents.read(buffer.clear()) >= 0) {
            algorithm.update(buffer.flip());
        }

        return algorithm.getValue();

    }

    /**
     * Defines a Checksum-able {@link Struct} type.
     */
    public interface Checkable {

        /**
         * Returns the member of the struct containing the checksum. Calculation presumes that for verification this
         * member will be set to zero before calculating the checksum, and that this value will be skipped when
         * verifying the checksum.
         *
         * Additionally, the checker and validator both assume that this member falls somewhere inside the
         * {@link ByteBuffer} returned by {@link #contentsToCheck()}.
         *
         * @return the checksum member
         */
        Struct.Unsigned32 checksum();

        /**
         * Returns a {@link ByteBuffer} of the {@link Struct}'s contents positioned appropriately for the the algorithm.
         * The returned value must have the position and limit set appropriately to perform the checksum calculation.
         *
         * The position must be the beginning of the {@link Struct} and the limit must be set to the final region of
         * data to check (which may exceed the size of the {@link Struct}).
         *
         * @return the {@link ByteBuffer}
         */
        default ByteBuffer contentsToCheck() {
            final var struct = checksum().struct();
            final var byteBuffer = struct.getByteBuffer();
            final var position = struct.getByteBufferPosition();
            return byteBuffer.position(position).limit(position + struct.size());
        }

    }

    /**
     * Defines a Checksum-able type which is backed by a {@link SeekableByteChannel} or subclass. This for when a header
     * preceds
     */
    public interface ChannelCheckable {

        /**
         * Returns a {@link SeekableByteChannel} of the contents to check.
         *
         * @return the {@link ByteBuffer}
         */
        SeekableByteChannel contentsToCheck() throws IOException;

        /**
         * Allocates a {@link ByteBuffer} used to check the contents. This uses
         * @return a {@link ByteBuffer} to use intermediately while calculating the checksum.
         */
        default ByteBuffer getIntermediateBuffer() {
            return ByteBuffer.allocate(4096);
        }

    }

    protected interface Algorithm {
        void update(byte b);
        void update(ByteBuffer byteBuffer);
        long getValue();
    }

}
