package com.namazustudios.socialengine.rt.transact.unix;

import java.nio.ByteBuffer;

public class UnixFSTransactionProgram {

    final ByteBuffer byteBuffer;

    final UnixFSTransactionProgramHeader header = new UnixFSTransactionProgramHeader();

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
    public UnixFSTransactionProgram commit(final UnixFSTransactionProgramExecutionPhase... executionPhases) {

        short phases = 0;

        for (final UnixFSTransactionProgramExecutionPhase executionPhase : executionPhases) {
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

}
