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
        public void compute(UnixFSTransactionProgram program) {
            final Adler32 crc32 = new Adler32();

            program.byteBuffer.position(program.header.getByteBufferPosition());
            program.byteBuffer.limit((int)program.header.length.get());
            program.header.checksum.set(0);

            crc32.update(program.byteBuffer);
            program.header.checksum.set(crc32.getValue());

        }
    };

    public abstract void compute(final UnixFSTransactionProgram program);

}
