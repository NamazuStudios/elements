package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgramExecutionPhase.CLEANUP;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgramExecutionPhase.COMMIT;

public class UnixFSTransactionProgramInterpreter {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSTransactionProgramInterpreter.class);

    final UnixFSTransactionProgram program;

    final List<UnixFSTransactionCommand> commits;

    final List<UnixFSTransactionCommand> cleanups;

    UnixFSTransactionProgramInterpreter(final UnixFSTransactionProgram program,
                                        final List<UnixFSTransactionCommand> commits,
                                        final List<UnixFSTransactionCommand> cleanups) {
        this.program = program;
        this.commits = commits;
        this.cleanups = cleanups;
    }

    /**
     * Executes the commit phase.
     *
     * @param executionHandler
     */
    public void executeCommitPhase(final ExecutionHandler executionHandler) {

        final short phases = program.header.phases.get();

        if (((0x1 << COMMIT.ordinal()) & phases) == 0) {
            throw new IllegalStateException(COMMIT + " hot valid.");
        }

        commits.forEach(command -> interpret(command, executionHandler));

    }

    /**
     * Executes the cleanup phase.
     *
     * @param executionHandler
     */
    public void executeCleanupPhase(final ExecutionHandler executionHandler) {

        final short phases = program.header.phases.get();

        if (((0x1 << CLEANUP.ordinal()) & phases) == 0) {
            throw new IllegalStateException(CLEANUP + " hot valid.");
        }

        cleanups.forEach(command -> interpret(command, executionHandler));

    }

    private void interpret(final UnixFSTransactionCommand command, final ExecutionHandler executionHandler) {

        final UnixFSTransactionCommandInstruction instruction = command.header.instruction.get();
        logger.trace("Command {}", command);

        switch (command.header.instruction.get()) {
            case NOOP:
                break;
            case UNLINK_FS_PATH:
                unlinkFSPath(command, executionHandler);
                break;
            case UNLINK_RT_PATH:
                unlinkRTPath(command, executionHandler);
                break;
            case REMOVE_RESOURCE:
                removeResource(command, executionHandler);
                break;
            case LINK_RESOURCE_TO_RT_PATH:
                linkResourceToRTPath(command, executionHandler);
                break;
            case UPDATE_RESOURCE:
                updateResource(command, executionHandler);
                break;
            case LINK_NEW_RESOURCE:
                linkNewResource(command, executionHandler);
                break;
            default:
                throw new InternalException("Unknown instruction: " + instruction);
        }
    }

    private void unlinkFSPath(final UnixFSTransactionCommand command,
                              final ExecutionHandler executionHandler) {
        final java.nio.file.Path fsPath = command.getParameterAt(0).asFSPath();
        executionHandler.unlinkFile(program, fsPath);
    }

    private void unlinkRTPath(final UnixFSTransactionCommand command,
                              final ExecutionHandler executionHandler) {
        final com.namazustudios.socialengine.rt.Path rtPath = command.getParameterAt(0).asRTPath();
        executionHandler.unlinkRTPath(program, rtPath);
    }

    private void removeResource(final UnixFSTransactionCommand command,
                                final ExecutionHandler executionHandler) {
        final ResourceId resourceId = command.getParameterAt(0).asResourceId();
        executionHandler.removeResource(program, resourceId);
    }

    private void linkResourceToRTPath(final UnixFSTransactionCommand command,
                                      final ExecutionHandler executionHandler) {
        final ResourceId resourceId = command.getParameterAt(0).asResourceId();
        final com.namazustudios.socialengine.rt.Path rtPath = command.getParameterAt(1).asRTPath();
        executionHandler.linkResourceToRTPath(program, resourceId, rtPath);
    }

    private void updateResource(final UnixFSTransactionCommand command,
                                final ExecutionHandler executionHandler) {
        final java.nio.file.Path fsPath = command.getParameterAt(0).asFSPath();
        final ResourceId resourceId = command.getParameterAt(1).asResourceId();
        executionHandler.updateResource(program, fsPath, resourceId);
    }

    private void linkNewResource(final UnixFSTransactionCommand command,
                                 final ExecutionHandler executionHandler) {
        final java.nio.file.Path fsPath = command.getParameterAt(0).asFSPath();
        final ResourceId resourceId = command.getParameterAt(1).asResourceId();
        executionHandler.linkNewResource(program, fsPath, resourceId);
    }

    /**
     * Implements the execution handlers.
     */
    interface ExecutionHandler {

        /**
         * Handles {@link UnixFSTransactionCommandInstruction#UNLINK_FS_PATH}
         *
         * @param program
         * @param fsPath
         */
        void unlinkFile(UnixFSTransactionProgram program, Path fsPath);

        /**
         * Handles {@link UnixFSTransactionCommandInstruction#UNLINK_RT_PATH}
         *
         * @param program
         * @param rtPath
         */
        void unlinkRTPath(UnixFSTransactionProgram program, com.namazustudios.socialengine.rt.Path rtPath);

        /**
         * Handles {@link UnixFSTransactionCommandInstruction#REMOVE_RESOURCE}
         *
         * @param program
         * @param resourceId
         */
        void removeResource(UnixFSTransactionProgram program, ResourceId resourceId);

        /**
         * Handles {@link UnixFSTransactionCommandInstruction#LINK_RESOURCE_TO_RT_PATH}
         *
         * @param program
         * @param resourceId
         * @param rtPath
         */
        void linkResourceToRTPath(UnixFSTransactionProgram program,
                                  ResourceId resourceId,
                                  com.namazustudios.socialengine.rt.Path rtPath);

        /**
         * Handles {@link UnixFSTransactionCommandInstruction#UPDATE_RESOURCE}
         * @param program
         * @param fsPath
         * @param resourceId
         */
        void updateResource(UnixFSTransactionProgram program,
                            Path fsPath,
                            ResourceId resourceId);

        /**
         * Handles {@link UnixFSTransactionCommandInstruction#LINK_NEW_RESOURCE}
         * @param program
         * @param fsPath
         * @param resourceId
         */
        void linkNewResource(UnixFSTransactionProgram program,
                             Path fsPath,
                             ResourceId resourceId);

    }

}
