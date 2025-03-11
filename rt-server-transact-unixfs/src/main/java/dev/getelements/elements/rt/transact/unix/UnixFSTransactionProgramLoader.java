package dev.getelements.elements.rt.transact.unix;

import javolution.io.Struct.Unsigned32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import static dev.getelements.elements.rt.transact.unix.UnixFSTransactionProgramExecutionPhase.CLEANUP;
import static dev.getelements.elements.rt.transact.unix.UnixFSTransactionProgramExecutionPhase.COMMIT;

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

        if (!program.isValid())throw new UnixFSProgramCorruptionException("Invalid program.");

        program.getByteBuffer().clear();
        commits = load(COMMIT, program.header.commitPos, program.header.commitLen);
        cleanups = load(CLEANUP, program.header.cleanupPos, program.header.cleanupLen);

        final var phaseMap = new EnumMap<UnixFSTransactionProgramExecutionPhase, List<UnixFSTransactionCommand>>(UnixFSTransactionProgramExecutionPhase.class);
        phaseMap.put(COMMIT, commits);
        phaseMap.put(CLEANUP, cleanups);

        return new UnixFSTransactionProgramInterpreter(program, phaseMap);

    }


    private List<UnixFSTransactionCommand> load(
            final UnixFSTransactionProgramExecutionPhase executionPhase,
            final Unsigned32 pos,
            final Unsigned32 len) {

        final long lPos = pos.get();
        final long lLimit = pos.get() + len.get();

        if (lPos > Integer.MAX_VALUE || lPos < 0) {
            throw new UnixFSProgramCorruptionException(executionPhase + " bad program segment position " + lPos);
        }

        if (lLimit > Integer.MAX_VALUE || lLimit < 0) {
            throw new UnixFSProgramCorruptionException(executionPhase + " bad program segment length " + lPos);
        }

        return load((int)lPos, (int)lLimit);

    }

    private List<UnixFSTransactionCommand> load(int position, int limit) {

        final var programSlice = program.getByteBuffer()
                .duplicate()
                .position(position)
                .limit(limit);

        final var commands = new ArrayList<UnixFSTransactionCommand>();

        while (programSlice.hasRemaining()) {
            final UnixFSTransactionCommand command = UnixFSTransactionCommand.from(programSlice);
            logger.trace("Loaded command {} {}.", program.header.transactionId.get(), command);
            commands.add(command);
        }

        return commands;

    }

}
