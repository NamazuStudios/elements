package com.namazustudios.socialengine.rt.transact.unix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class UnixFSTransactionProgram {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSTransactionProgram.class);

    final ByteBuffer byteBuffer;

    final UnixFSTransactionProgramHeader header = new UnixFSTransactionProgramHeader();

    transient UnixFSTransactionProgramInterpreter interpreter = null;

    /**
     * Creates an instance with the bytebuffer assuming that the program's position is 0 in the supplied
     * {@link ByteBuffer}
     *
     * @param byteBuffer the {@link ByteBuffer}
     */
    UnixFSTransactionProgram(final ByteBuffer byteBuffer) {
        this(byteBuffer, 0);
    }

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
    public UnixFSTransactionProgram commit() {
        header.algorithm.get().compute(header);
        return this;
    }

    /**
     * Checks if this {@link UnixFSTransactionProgram}'s header is valid.
     * @return
     */
    public boolean isValid() {

        final UnixFSChecksumAlgorithm algorithm;

        try {
            algorithm = header.algorithm.get();
        } catch (ArrayIndexOutOfBoundsException ex) {
            logger.trace("Unable to determine checksum algorithm.", ex);
            return false;
        }

        return algorithm.isValid(header);

    }

    /**
     * Creates and instance of {@link UnixFSTransactionProgramInterpreter} to load the program in-memory and
     * subsequently execute the program.
     *
     * @return the {@link UnixFSTransactionProgramInterpreter}, or a cached instance.
     */
    public UnixFSTransactionProgramInterpreter interpreter() {

        if (interpreter == null) {
            final UnixFSTransactionProgramLoader loader = new UnixFSTransactionProgramLoader(this);
            interpreter = loader.load();
        }

        return interpreter;

    }

}
