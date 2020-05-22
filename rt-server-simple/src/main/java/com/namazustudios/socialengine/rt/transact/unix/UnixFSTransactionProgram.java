package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;

import java.nio.ByteBuffer;

public class UnixFSTransactionProgram {

    final ByteBuffer byteBuffer;

    final Header header = new Header();

    /**
     * Creates an instance with the bytebuffer and the program's position within the supplied {@link ByteBuffer}
     *
     * @param byteBuffer
     * @param programPosition
     */
    UnixFSTransactionProgram(final ByteBuffer byteBuffer, final int programPosition) {
        this.byteBuffer = byteBuffer;
        header.setByteBuffer(byteBuffer, programPosition);
    }

    /**
     * Commits this {@link UnixFSTransactionProgram} by calculating the checksum and setting it's
     */
    public void commit() {
        header.algorithm.get().compute(this);
    }

    /**
     * Indicates the phase of the a
     */
    public enum ExecutionPhase {

        /**
         * Happens in the commit phase.
         */
        COMMIT,

        /**
         * Happens in the cleanup phase.
         */
        CLEANUP

    }

    static class Header extends Struct {

        public static final int SIZE = new Header().size();

        final Enum8<UnixFSChecksumAlgorithm> algorithm = new Enum8<>(UnixFSChecksumAlgorithm.values());

        final Unsigned32 checksum = new Unsigned32();

        final Unsigned32 commitPos = new Unsigned32();

        final Unsigned32 commitLen = new Unsigned32();

        final Unsigned32 cleanupPos = new Unsigned32();

        final Unsigned32 cleanupLen = new Unsigned32();

    }

}
