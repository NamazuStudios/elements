package com.namazustudios.socialengine.rt.transact.unix;

import javolution.io.Struct.Unsigned32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgramExecutionPhase.CLEANUP;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgramExecutionPhase.COMMIT;

/**
 * Used to load an instance of {@link UnixFSTransactionProgram} and build an instance of
 * {@link UnixFSTransactionProgramInterpreter}, which can be used to execute all the commands in sequence.
 */
class UnixFSTransactionProgramLoader {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSTransactionProgramLoader.class);

    private final UnixFSTransactionProgram program;

    /**
     * Creates an instance of {@link UnixFSTransactionProgramLoader} which can build the
     * {@link UnixFSTransactionProgramInterpreter}
     * @param program the {@link UnixFSTransactionProgram} to load
     */
    UnixFSTransactionProgramLoader(final UnixFSTransactionProgram program) {
        this.program = program;
    }

    /**
     * Parses the {@link UnixFSTransactionProgram}'s contents and builds a {@link UnixFSTransactionProgramInterpreter}
     * which can be used to execute the underlying {@link UnixFSTransactionCommand}.
     *
     * @return the {@link UnixFSTransactionProgramInterpreter}
     */
    public UnixFSTransactionProgramInterpreter load() {

        final List<UnixFSTransactionCommand> commits, cleanups;
        final int programPosition = program.header.getByteBufferPosition();
        if (!program.isValid())throw new UnixFSProgramCorruptionException("Invalid program.");

        commits = load(COMMIT, programPosition, program.header.commitPos, program.header.commitLen);
        cleanups = load(CLEANUP, programPosition, program.header.cleanupPos, program.header.cleanupLen);
        return new UnixFSTransactionProgramInterpreter(program, commits, cleanups);

    }


    private List<UnixFSTransactionCommand> load(
            final UnixFSTransactionProgramExecutionPhase executionPhase,
            final int programPosition,
            final Unsigned32 pos,
            final Unsigned32 len) {

        final long lPos = pos.get() + programPosition;
        final long lLimit = pos.get() + len.get() + programPosition;

        if (lPos > Integer.MAX_VALUE || lPos < 0) {
            throw new UnixFSProgramCorruptionException(executionPhase + " bad program segment position " + lPos);
        }

        if (lLimit > Integer.MAX_VALUE || lLimit < 0) {
            throw new UnixFSProgramCorruptionException(executionPhase + " bad program segment length " + lPos);
        }

        return load((int)lPos, (int)lLimit);

    }

    private List<UnixFSTransactionCommand> load(int position, int limit) {

        program.byteBuffer.position(position).limit(limit);

        final List<UnixFSTransactionCommand> commands = new ArrayList<>();

        while (program.byteBuffer.hasRemaining()) {
            final UnixFSTransactionCommand command = UnixFSTransactionCommand.from(program.byteBuffer);
            commands.add(command);
        }

        return commands;

    }

}
