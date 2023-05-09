package dev.getelements.elements.rt.transact.unix;

/**
 * Indicates the phase of the transactional program.
 */
public enum UnixFSTransactionProgramExecutionPhase {

    /**
     * Happens in the commit phase.
     */
    COMMIT,

    /**
     * Happens in the cleanup phase. This should be executed regardless of rollback or commit.
     */
    CLEANUP

}
