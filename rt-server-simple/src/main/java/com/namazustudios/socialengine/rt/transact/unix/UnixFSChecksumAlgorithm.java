package com.namazustudios.socialengine.rt.transact.unix;

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
        public void verify(final UnixFSTransactionProgram program) throws ChecksumFailureExeception {

            final CRC32 crc32 = new CRC32();
            final int programPosition = program.header.getByteBufferPosition();

            program.byteBuffer.position(programPosition);
            program.byteBuffer.limit(programPosition + program.header.checksum.offset());

            crc32.update(program.byteBuffer);
            for (int i = 0; i < 4; ++i) crc32.update(0);

            program.byteBuffer.position(programPosition + program.header.phases.offset());
            program.byteBuffer.limit((int) (programPosition + program.header.length.get()));
            crc32.update(program.byteBuffer);

            if (crc32.getValue() != program.header.checksum.get()) throw new ChecksumFailureExeception();

        }

        @Override
        public void compute(final UnixFSTransactionProgram program) {

            final CRC32 crc32 = new CRC32();

            program.byteBuffer.position(program.header.getByteBufferPosition());
            program.byteBuffer.limit((int)program.header.length.get());
            program.header.checksum.set(0);

            crc32.update(program.byteBuffer);
            program.header.checksum.set(crc32.getValue());

        }
    },

    /**
     * Uses {@link Adler32}
     */
    ADLER_32 {

        @Override
        public void verify(UnixFSTransactionProgram program) throws ChecksumFailureExeception {

            final Adler32 adler32 = new Adler32();
            final int programPosition = program.header.getByteBufferPosition();

            program.byteBuffer.position(programPosition);
            program.byteBuffer.limit(programPosition + program.header.checksum.offset());

            adler32.update(program.byteBuffer);
            for (int i = 0; i < 4; ++i) adler32.update(0);

            program.byteBuffer.position(programPosition + program.header.phases.offset());
            program.byteBuffer.limit((int) (programPosition + program.header.length.get()));
            adler32.update(program.byteBuffer);

            if (adler32.getValue() != program.header.checksum.get()) throw new ChecksumFailureExeception();
        }

        @Override
        public void compute(UnixFSTransactionProgram program) {

            final Adler32 adler32 = new Adler32();

            program.byteBuffer.position(program.header.getByteBufferPosition());
            program.byteBuffer.limit((int)program.header.length.get());
            program.header.checksum.set(0);

            adler32.update(program.byteBuffer);
            program.header.checksum.set(adler32.getValue());

        }
    };

    /**
     * Computes the checksum, skipping the value of {@link UnixFSTransactionProgram.Header#checksum} and then compares
     * the computed value against the stored value. In the even of a mismatch this will throw an instance of
     * {@link ChecksumFailureExeception}
     *
     * @param program the program to verify
     *
     * @throws ChecksumFailureExeception if the checksum mismatches
     */
    public abstract void verify(final UnixFSTransactionProgram program) throws ChecksumFailureExeception;

    /**
     * Computes the checksum and then sets the {@link UnixFSTransactionProgram.Header#checksum} value.
     *
     * @param program the program for which to compute the checksum
     */
    public abstract void compute(final UnixFSTransactionProgram program) throws ChecksumFailureExeception;

}
