package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class UnixFSTransactionProgram {

    final ByteBuffer byteBuffer;

    final Header header = new Header();

    transient boolean valid = false;

    transient UnixFSTransactionProgramInterpreter interpreter = null;

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
    public UnixFSTransactionProgram commit(final ExecutionPhase ... executionPhases) {

        short phases = 0;

        for (final ExecutionPhase executionPhase : executionPhases) {
            phases |= 0x1 << executionPhase.ordinal();
        }

        header.phases.set(phases);
        header.algorithm.get().compute(this);
        valid = true;

        return this;

    }

    /**
     * Creates and instance of {@link UnixFSTransactionProgramInterpreter} to load the program in-memory and
     * subsequently execute the program.
     *
     * @return the {@link UnixFSTransactionProgramInterpreter}, or a cached instance.
     */
    UnixFSTransactionProgramInterpreter interpreter() {

        if (interpreter == null) {
            final UnixFSTransactionProgramLoader loader = new UnixFSTransactionProgramLoader(this);
            interpreter = loader.load();
            valid = true;
        } else if (!valid) {
            throw new UnixFSProgramCorruptionException("Invalid program.");
        }

        return interpreter;

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

        final Unsigned8 phases = new Unsigned8();

        final Unsigned32 length = new Unsigned32();

        final Unsigned32 commitPos = new Unsigned32();

        final Unsigned32 commitLen = new Unsigned32();

        final Unsigned32 cleanupPos = new Unsigned32();

        final Unsigned32 cleanupLen = new Unsigned32();

    }

}
