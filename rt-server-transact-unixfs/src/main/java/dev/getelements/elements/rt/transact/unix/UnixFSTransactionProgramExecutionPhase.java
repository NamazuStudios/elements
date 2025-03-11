package dev.getelements.elements.rt.transact.unix;

import java.util.stream.Stream;

/**
 * Indicates the phase of the transactional program.
 */
public enum UnixFSTransactionProgramExecutionPhase {

    /**
     * This executes the operations needed to perform the transaction, including all writes to the datastore.
     */
    COMMIT,

    /**
     * Happens in the cleanup phase. This should be executed regardless of rollback or commit.
     */
    CLEANUP;

    /**
     * Given the bitmask, returns true if this phase is enabled.
     *
     * @param phases the phases
     * @return true if enabled
     */
    public boolean isEnabled(final long phases) {
        return (phases & 0x1 << ordinal()) != 0;
    }

    /**
     * Given the bitmask, returns true if this phase is disabled.
     *
     * @param phases the phases
     * @return true if disabled
     */
    public boolean isDisabled(final long phases) {
        return !isEnabled(phases);
    }

    /**
     * Returns all matching phases.
     *
     * @param bitmask the bitmask
     * @return the bitmask
     */
    static Stream<UnixFSTransactionProgramExecutionPhase> enabledPhasesFor(final long bitmask) {
        return Stream.of(values()).filter(executionPhase -> executionPhase.isEnabled(bitmask));
    }

}
