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
        executionHandler.getLogger().trace("BEGIN Processing Command {}", command);

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
            case ADD_PATH:
                addPath(command, executionHandler);
                break;
            case ADD_RESOURCE_ID:
                addResourceId(command, executionHandler);
                break;
            case LINK_NEW_RESOURCE:
                linkNewResource(command, executionHandler);
                break;
            default:
                throw new InternalException("Unknown instruction: " + instruction);
        }

        executionHandler.getLogger().trace("END Processing Command", command);

    }

    private void unlinkFSPath(final UnixFSTransactionCommand command,
                              final ExecutionHandler executionHandler) {
        final var fsPath = command.getParameterAt(0).asFSPath();
        executionHandler.unlinkFile(program, command, fsPath);
    }

    private void unlinkRTPath(final UnixFSTransactionCommand command,
                              final ExecutionHandler executionHandler) {
        final var resourceId = command.getParameterAt(0).asResourceId();
        final var rtPath = command.getParameterAt(1).asRTPath();
        executionHandler.unlinkRTPath(program, command, resourceId, rtPath);
    }

    private void removeResource(final UnixFSTransactionCommand command,
                                final ExecutionHandler executionHandler) {
        final var resourceId = command.getParameterAt(0).asResourceId();
        executionHandler.removeResource(program, command, resourceId);
    }

    private void linkResourceToRTPath(final UnixFSTransactionCommand command,
                                      final ExecutionHandler executionHandler) {
        final var resourceId = command.getParameterAt(0).asResourceId();
        final var rtPath = command.getParameterAt(1).asRTPath();
        executionHandler.linkResourceToRTPath(program, command, resourceId, rtPath);
    }

    private void updateResource(final UnixFSTransactionCommand command,
                                final ExecutionHandler executionHandler) {
        final var fsPath = command.getParameterAt(0).asFSPath();
        final var resourceId = command.getParameterAt(1).asResourceId();
        executionHandler.updateResource(program, command, fsPath, resourceId);
    }

    private void addPath(final UnixFSTransactionCommand command,
                         final ExecutionHandler executionHandler) {
        final com.namazustudios.socialengine.rt.Path path = command.getParameterAt(0).asRTPath();
        executionHandler.addPath(program, command, path);
    }

    private void addResourceId(final UnixFSTransactionCommand command,
                               final ExecutionHandler executionHandler) {
        final var resourceId = command.getParameterAt(0).asResourceId();
        executionHandler.addResourceId(program, command, resourceId);
    }

    private void linkNewResource(final UnixFSTransactionCommand command,
                                 final ExecutionHandler executionHandler) {
        final var fsPath = command.getParameterAt(0).asFSPath();
        final var resourceId = command.getParameterAt(1).asResourceId();
        executionHandler.linkNewResource(program, command, fsPath, resourceId);
    }

    /**
     * Implements the execution handlers.
     */
    interface ExecutionHandler {

        /**
         * Handles {@link UnixFSTransactionCommandInstruction#UNLINK_FS_PATH}
         * @param program
         * @param command
         * @param fsPath
         */
        default void unlinkFile(final UnixFSTransactionProgram program,
                                final UnixFSTransactionCommand command,
                                final Path fsPath) {
            getLogger().trace("Ignoring {}", command);
        }

        /**
         * Handles {@link UnixFSTransactionCommandInstruction#UNLINK_RT_PATH}
         * @param program
         * @param command
         * @param rtPath
         */
        default void unlinkRTPath(final UnixFSTransactionProgram program,
                                  final UnixFSTransactionCommand command,
                                  final ResourceId resourceId,
                                  final com.namazustudios.socialengine.rt.Path rtPath) {
            getLogger().trace("Ignoring {}", command);
        }

        /**
         * Handles {@link UnixFSTransactionCommandInstruction#REMOVE_RESOURCE}
         * @param program
         * @param command
         * @param resourceId
         */
        default void removeResource(final UnixFSTransactionProgram program,
                                    final UnixFSTransactionCommand command,
                                    final ResourceId resourceId) {
            getLogger().trace("Ignoring {}", command);
        }

        /**
         * Handles {@link UnixFSTransactionCommandInstruction#LINK_RESOURCE_TO_RT_PATH}
         *
         * @param program
         * @param command
         * @param resourceId
         * @param rtPath
         */
        default void linkResourceToRTPath(final UnixFSTransactionProgram program,
                                          final UnixFSTransactionCommand command, ResourceId resourceId,
                                          final com.namazustudios.socialengine.rt.Path rtPath) {
            getLogger().trace("Ignoring {}", command);
        }

        /**
         * Handles {@link UnixFSTransactionCommandInstruction#UPDATE_RESOURCE}
         * @param program
         * @param command
         * @param fsPath
         * @param resourceId
         */
        default void updateResource(final UnixFSTransactionProgram program,
                                    final UnixFSTransactionCommand command,
                                    final Path fsPath,
                                    final ResourceId resourceId) {
            getLogger().trace("Ignoring {}", command);
        }

        /**
         * Handles the {@link UnixFSTransactionCommandInstruction#ADD_PATH}.
         *  @param program
         * @param command
         * @param path
         */
        default void addPath(final UnixFSTransactionProgram program,
                             final UnixFSTransactionCommand command,
                             final com.namazustudios.socialengine.rt.Path path) {
            getLogger().trace("Ignoring {}", command);
        }

        /**
         * Handles the {@link UnixFSTransactionCommandInstruction#ADD_RESOURCE_ID}.
         * @param program
         * @param command
         * @param resourceId
         */
        default void addResourceId(final UnixFSTransactionProgram program,
                                   final UnixFSTransactionCommand command,
                                   final ResourceId resourceId) {
            getLogger().trace("Ignoring {}", command);
        }

        /**
         * Handles {@link UnixFSTransactionCommandInstruction#LINK_NEW_RESOURCE}
         * @param program
         * @param command
         * @param fsPath
         * @param resourceId
         */
        default void linkNewResource(final UnixFSTransactionProgram program,
                                     final UnixFSTransactionCommand command,
                                     final Path fsPath,
                                     final ResourceId resourceId) {
            getLogger().trace("Ignoring {}", command);
        }

        /**
         * Returns the preferred logger for this {@link ExecutionHandler}. By default, all instructions are logged at
         * trace level with the supplied {@link Logger}.
         *
         * @return the logger
         */
        default Logger getLogger() {
            return LoggerFactory.getLogger(this.getClass());
        }

    }

}
