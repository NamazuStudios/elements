package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionCommand.Instruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgram.ExecutionPhase.CLEANUP;
import static com.namazustudios.socialengine.rt.transact.unix.UnixFSTransactionProgram.ExecutionPhase.COMMIT;

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

        final Instruction instruction = command.header.instruction.get();
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
            case LINK_RESOURCE_FILE_TO_RT_PATH:
                linkFSPathToRTPath(command, executionHandler);
                break;
            case LINK_RESOURCE_TO_RT_PATH:
                linkResourceToRTPath(command, executionHandler);
                break;
            case LINK_RESOURCE_FILE_TO_RESOURCE_ID:
                linkFSPathToResourceId(command, executionHandler);
                break;
            default:
                throw new InternalException("Unknown instruction: " + instruction);
        }
    }

    private void unlinkFSPath(final UnixFSTransactionCommand command,
                              final ExecutionHandler executionHandler) {
        final java.nio.file.Path fsPath = command.getParameterAt(0).asFSPath();
        executionHandler.unlinkFile(fsPath);
    }

    private void unlinkRTPath(final UnixFSTransactionCommand command,
                              final ExecutionHandler executionHandler) {
        final com.namazustudios.socialengine.rt.Path rtPath = command.getParameterAt(0).asRTPath();
        executionHandler.unlinkRTPath(rtPath);
    }

    private void removeResource(final UnixFSTransactionCommand command,
                                final ExecutionHandler executionHandler) {
        final ResourceId resourceId = command.getParameterAt(0).asResourceId();
        executionHandler.removeResource(resourceId);
    }

    private void linkFSPathToRTPath(final UnixFSTransactionCommand command,
                                    final ExecutionHandler executionHandler) {
        final java.nio.file.Path fsPath = command.getParameterAt(0).asFSPath();
        final com.namazustudios.socialengine.rt.Path rtPath = command.getParameterAt(1).asRTPath();
        executionHandler.linkFSPathToRTPath(fsPath, rtPath);
    }

    private void linkResourceToRTPath(final UnixFSTransactionCommand command,
                                      final ExecutionHandler executionHandler) {
        final ResourceId resourceId = command.getParameterAt(0).asResourceId();
        final com.namazustudios.socialengine.rt.Path rtPath = command.getParameterAt(1).asRTPath();
        executionHandler.linkResourceToRTPath(resourceId, rtPath);
    }

    private void linkFSPathToResourceId(final UnixFSTransactionCommand command,
                                        final ExecutionHandler executionHandler) {
        final java.nio.file.Path fsPath = command.getParameterAt(0).asFSPath();
        final ResourceId resourceId = command.getParameterAt(1).asResourceId();
        executionHandler.linkFSPathToResourceId(fsPath, resourceId);
    }

    /**
     * Implements the execution handlers.
     */
    interface ExecutionHandler {

        /**
         * Handles {@link Instruction#UNLINK_FS_PATH}
         *
         * @param fsPath
         */
        void unlinkFile(Path fsPath);

        /**
         * Handles {@link Instruction#UNLINK_RT_PATH}
         *
         * @param rtPath
         */
        void unlinkRTPath(com.namazustudios.socialengine.rt.Path rtPath);

        /**
         * Handles {@link Instruction#REMOVE_RESOURCE}
         *
         * @param resourceId
         */
        void removeResource(ResourceId resourceId);

        /**
         * Handles {@link Instruction#LINK_RESOURCE_FILE_TO_RT_PATH}
         *
         * @param fsPath
         * @param rtPath
         */
        void linkFSPathToRTPath(java.nio.file.Path fsPath, com.namazustudios.socialengine.rt.Path rtPath);

        /**
         * Handles {@link Instruction#LINK_RESOURCE_TO_RT_PATH}
         *
         * @param resourceId
         * @param rtPath
         */
        void linkResourceToRTPath(ResourceId resourceId, com.namazustudios.socialengine.rt.Path rtPath);

        /**
         * Handles {@link Instruction#LINK_RESOURCE_FILE_TO_RESOURCE_ID}
         * @param fsPath
         * @param resourceId
         */
        void linkFSPathToResourceId(Path fsPath, ResourceId resourceId);

    }

}
