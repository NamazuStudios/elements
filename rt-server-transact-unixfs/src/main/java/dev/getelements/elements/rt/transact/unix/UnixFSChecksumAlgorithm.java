package dev.getelements.elements.rt.transact.unix;

import javolution.io.Struct;

import java.lang.reflect.Member;
import java.nio.ByteBuffer;
import java.util.zip.Adler32;
import java.util.zip.CRC32;

/**
 * Which checksum algorithm to use when committing the transaction.
 */
public enum UnixFSChecksumAlgorithm {

    /**
     * Uses {@link CRC32}
     */
    CRC_32 {

        @Override
        public boolean isValid(final Checkable checkable) throws UnixFSChecksumFailureExeception {

            final CRC32 crc32 = new CRC32();

            return isValid(checkable, new Algorithm() {
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
            });

        }

        @Override
        public void compute(final Checkable checkable) {

            final CRC32 crc32 = new CRC32();

            final Struct.Unsigned32 checksum = checkable.checksum();
            final ByteBuffer contents = checkable.contentsToCheck();

            checksum.set(0);
            crc32.update(contents);

            final long value = crc32.getValue();
            checksum.set(value);

        }

    },

    /**
     * Uses {@link Adler32}
     */
    ADLER_32 {

        @Override
        public boolean isValid(final Checkable checkable) throws UnixFSChecksumFailureExeception {

            final Adler32 adler32 = new Adler32();

            return isValid(checkable, new Algorithm() {
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
            });

        }

        @Override
        public void compute(final Checkable checkable) {

            final Adler32 adler32 = new Adler32();

            final Struct.Unsigned32 checksum = checkable.checksum();
            final ByteBuffer contents = checkable.contentsToCheck();

            checksum.set(0);
            adler32.update(contents);

            final long value = adler32.getValue();
            checksum.set(value);

        }

    };

    protected boolean isValid(final Checkable checkable, final Algorithm algorithm) {

        final ByteBuffer contents = checkable.contentsToCheck();
        final Struct.Unsigned32 checksum = checkable.checksum();

        // Original limit/position values of the buffer
        final int limit = contents.limit();
        final int position = contents.position();

        // Sets to read up to the beginning of the checksum member and updates the checksum.
        if (checksum.offset() > 0) {
            contents.limit(position + checksum.offset());
            algorithm.update(contents);
        }

        // Inserts four zero bytes as if it were calculated with zeros in that position
        for (int i = 0; i < 4; ++i) algorithm.update((byte) 0x0);

        // Sets the limit and position to the remainder of the buffer and updates the checksum.
        contents.position(4 + position + checksum.offset()).limit(limit);
        algorithm.update(contents);

        final long existing = checksum.get();
        final long calculated = algorithm.getValue();

        return existing == calculated;

    }

    /**
     * Checks if this the supplied {@link Checkable} is valid.
     *
     * @param checkable
     * @return true if valid, false otherwise
     */
    public abstract boolean isValid(final Checkable checkable);

    /**
     * Computes the checksum, skipping the value of {@link Checkable#checksum()} and then compares the computed value
     * against the stored value. In the even of a mismatch this will throw an instance of
     * {@link UnixFSChecksumFailureExeception}
     *
     * @param checkable the {@link Checkable} to verify
     *
     * @throws UnixFSChecksumFailureExeception if the checksum mismatches
     */
    public void verify(final Checkable checkable) throws UnixFSChecksumFailureExeception {
        if (!isValid(checkable)) throw new UnixFSChecksumFailureExeception("Checksum failure.");
    }

    /**
     * Computes the checksum and then sets the {@link Checkable#checksum()} value.
     *
     * @param checkable the structure to compute
     */
    public abstract void compute(final Checkable checkable) throws UnixFSChecksumFailureExeception;

    /**
     * Defines a Checksum-able {@link Struct} type.
     */
    interface Checkable {

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
        ByteBuffer contentsToCheck();

    }

    private interface Algorithm {
        void update(byte b);
        void update(ByteBuffer byteBuffer);
        long getValue();
    }
}
