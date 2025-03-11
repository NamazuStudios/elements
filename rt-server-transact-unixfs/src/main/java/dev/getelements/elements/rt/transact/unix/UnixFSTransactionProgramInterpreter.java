package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static dev.getelements.elements.rt.transact.unix.UnixFSTransactionProgramExecutionPhase.CLEANUP;
import static dev.getelements.elements.rt.transact.unix.UnixFSTransactionProgramExecutionPhase.COMMIT;
import static java.util.Collections.emptyList;

public class UnixFSTransactionProgramInterpreter {

    final UnixFSTransactionProgram program;

    final Map<UnixFSTransactionProgramExecutionPhase, List<UnixFSTransactionCommand>> phaseMap;

    UnixFSTransactionProgramInterpreter(
            final UnixFSTransactionProgram program,
            final Map<UnixFSTransactionProgramExecutionPhase, List<UnixFSTransactionCommand>> phaseMap) {
        this.program = program;
        this.phaseMap  = phaseMap;
    }

    /**
     * Executes the program with the supplied {@link UnixFSTransactionProgramExecutionPhase} and
     * {@link ExecutionHandler}. Throwing an exception if the phase isn't defined.
     *
     * @param phase the phase to execute
     * @param executionHandler the execution handler
     * @return this instance
     */
    public UnixFSTransactionProgramInterpreter execute(
            final UnixFSTransactionProgramExecutionPhase phase,
            final ExecutionHandler executionHandler) {

        final short phases = program.header.phases.get();

        if (phase.isDisabled(phases)) {
            throw new IllegalStateException(phase + " not valid.");
        }

        phaseMap.getOrDefault(phase, emptyList()).forEach(command -> interpret(command, executionHandler));

        return this;
    }

    /**
     * Executes the program with the supplied {@link UnixFSTransactionProgramExecutionPhase} and
     * {@link ExecutionHandler}. Logging a debug message if the phase isn't defined.
     *
     * @param phase the phase to execute
     * @param executionHandler the execution handler
     * @return this instance
     */
    public UnixFSTransactionProgramInterpreter tryExecute(
            final UnixFSTransactionProgramExecutionPhase phase,
            final ExecutionHandler executionHandler) {

        final short phases = program.header.phases.get();

        if (phase.isDisabled(phases)) {
            executionHandler.getLogger().info("No {} phase defined. Skipping.", phase);
        } else {
            phaseMap.getOrDefault(phase, emptyList()).forEach(command -> interpret(command, executionHandler));
        }

        return this;
    }

    /**
     * Executes the commit phase.
     *
     * @param executionHandler
     */
    public UnixFSTransactionProgramInterpreter executeCommitPhase(final ExecutionHandler executionHandler) {
        return execute(COMMIT, executionHandler);
    }

    /**
     * Executes the commit phase. If the phase is not defined, then this simply logs a message and skips the phase.
     *
     * @param executionHandler the {@link ExecutionHandler} to use
     */
    public UnixFSTransactionProgramInterpreter tryExecuteCommitPhase(final ExecutionHandler executionHandler) {
        return tryExecute(COMMIT, executionHandler);
    }

    /**
     * Executes the cleanup phase.
     *
     * @param executionHandler
     */
    public UnixFSTransactionProgramInterpreter executeCleanupPhase(final ExecutionHandler executionHandler) {
        return execute(CLEANUP, executionHandler);
    }

    /**
     * Executes the cleanup phase. If the phase is not defined, then this simply logs a message and skips the phase.
     *
     * @param executionHandler the {@link ExecutionHandler} to use
     */
    public UnixFSTransactionProgramInterpreter tryExecuteCleanupPhase(final ExecutionHandler executionHandler) {
        return tryExecute(CLEANUP, executionHandler);
    }

    private void interpret(final UnixFSTransactionCommand command, final ExecutionHandler executionHandler) {

        final UnixFSTransactionCommandInstruction instruction = command.header.instruction.get();
        executionHandler.getLogger().trace("BEGIN Processing Command {}", command);

        switch (command.header.instruction.get()) {
            case NOOP:
                executionHandler.getLogger().trace("NOOP");
                break;
            case APPLY_CONTENTS_CHANGE_FOR_RESOURCE:
                applyContentsChange(command, executionHandler);
                break;
            case APPLY_PATH_CHANGE_FOR_RESOURCE:
                applyPathChange(command, executionHandler);
                break;
            case APPLY_REVERSE_PATH_CHANGE_FOR_RESOURCE:
                applyResourceIdChange(command, executionHandler);
                break;
            case APPLY_TASK_CHANGES_FOR_RESOURCE_ID:
                applyTaskChanges(command, executionHandler);
                break;
            case CLEANUP_RESOURCE_AT_PATH:
                cleanupPath(command, executionHandler);
                break;
            case CLEANUP_RESOURCE_FOR_RESOURCE_ID:
                cleanupResourceId(command, executionHandler);
                break;
            case CLEANUP_TASKS_FOR_RESOURCE_ID:
                cleanupTasks(command, executionHandler);
                break;
            default:
                throw new InternalException("Unknown instruction: " + instruction);
        }

        executionHandler.getLogger().trace("END Processing Command {}", command);

    }

    private void applyContentsChange(final UnixFSTransactionCommand command,
                                     final ExecutionHandler executionHandler) {
        final var transactionId = command.getParameterAt(0).asString();
        final var resourceId = command.getParameterAt(1).asResourceId();
        executionHandler.applyContentsChange(program, command, resourceId, transactionId);
    }

    private void applyPathChange(final UnixFSTransactionCommand command,
                                 final ExecutionHandler executionHandler) {
        final var transactionId = command.getParameterAt(0).asString();
        final var rtPath = command.getParameterAt(1).asRTPath();
        executionHandler.applyPathChange(program, command, rtPath, transactionId);
    }

    private void applyResourceIdChange(final UnixFSTransactionCommand command,
                                       final ExecutionHandler executionHandler) {
        final var transactionId = command.getParameterAt(0).asString();
        final var resourceId = command.getParameterAt(1).asResourceId();
        executionHandler.applyReversePathsChange(program, command, resourceId, transactionId);
    }

    private void applyTaskChanges(final UnixFSTransactionCommand command,
                                  final ExecutionHandler executionHandler) {
        final var transactionId = command.getParameterAt(0).asString();
        final var resourceId = command.getParameterAt(1).asResourceId();
        executionHandler.applyTaskChanges(program, command, resourceId, transactionId);
    }

    private void cleanupPath(final UnixFSTransactionCommand command,
                             final ExecutionHandler executionHandler) {
        final var transactionId = command.getParameterAt(0).asString();
        final var rtPath = command.getParameterAt(1).asRTPath();
        executionHandler.cleanupPath(program, command, rtPath, transactionId);
    }

    private void cleanupResourceId(final UnixFSTransactionCommand command,
                                   final ExecutionHandler executionHandler) {
        final var transactionId = command.getParameterAt(0).asString();
        final var resourceId = command.getParameterAt(1).asResourceId();
        executionHandler.cleanupResourceId(program, command, resourceId, transactionId);
    }

    private void cleanupTasks(final UnixFSTransactionCommand command,
                              final ExecutionHandler executionHandler) {
        final var transactionId = command.getParameterAt(0).asString();
        final var resourceId = command.getParameterAt(1).asResourceId();
        executionHandler.cleanupTasks(program, command, resourceId, transactionId);
    }

    /**
     * Implements the execution handlers.
     */
    public interface ExecutionHandler {

        /**
         * Applies the change to the {@link ResourceId}.
         *
         * @param program the program
         * @param command the command
         * @param resourceId the {@link ResourceId}
         * @param transactionId the Transaction ID
         */
        default void applyReversePathsChange(final UnixFSTransactionProgram program,
                                             final UnixFSTransactionCommand command,
                                             final ResourceId resourceId,
                                             final String transactionId) {
            getLogger().trace("Ignoring {}", command);
        }

        /**
         * Applies the supplied {@link Path} change.
         *
         * @param program
         * @param command
         * @param rtPath
         * @param transactionId
         */
        default void applyPathChange(
                final UnixFSTransactionProgram program,
                final UnixFSTransactionCommand command,
                final Path rtPath,
                final String transactionId) {
            getLogger().trace("Ignoring {}", command);

        }
        /**
         * Applies the task changes to the datastore.
         *
         * @param program
         * @param command
         * @param resourceId
         * @param transactionId
         */
        default void applyTaskChanges(
                final UnixFSTransactionProgram program,
                final UnixFSTransactionCommand command,
                final ResourceId resourceId,
                final String transactionId) {
            getLogger().trace("Ignoring {}", command);
        }

        /**
         * Applies the contnets change to a {@link ResourceId}.
         *
         * @param program
         * @param command
         * @param resourceId
         * @param transactionId
         */
        default void applyContentsChange(
                final UnixFSTransactionProgram program,
                final UnixFSTransactionCommand command,
                final ResourceId resourceId,
                final String transactionId) {
            getLogger().trace("Ignoring {}", command);
        }

        /**
         * Cleans up the {@link ResourceId} resources associated with the transaction.
         *
         * @param program
         * @param command
         * @param resourceId
         * @param transactionId
         */
        default void cleanupResourceId(
                final UnixFSTransactionProgram program,
                final UnixFSTransactionCommand command,
                final ResourceId resourceId,
                final String transactionId) {
            getLogger().trace("Ignoring {}", command);
        }

        /**
         * Cleans up the {@link Path} resources associated with the transaction.
         *
         * @param program
         * @param command
         * @param rtPath
         * @param transactionId
         */
        default void cleanupPath(
                final UnixFSTransactionProgram program,
                final UnixFSTransactionCommand command,
                final Path rtPath,
                final String transactionId) {
            getLogger().trace("Ignoring {}", command);
        }

        /**
         * Cleans up the tasks for the {@link ResourceId} resources associated with the transaction.
         *
         * @param program
         * @param command
         * @param resourceId
         * @param transactionId
         */
        default void cleanupTasks(
                final UnixFSTransactionProgram program,
                final UnixFSTransactionCommand command,
                final ResourceId resourceId,
                final String transactionId) {
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
