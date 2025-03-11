package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.DataStore;

/**
 * Indicates the instruction.
 */
public enum UnixFSTransactionCommandInstruction {

    /**
     * No-op.
     */
    NOOP,

    /**
     * Adds the supplied {@link Path} changes from the current transaction to the
     * {@link DataStore}.
     **/
    APPLY_PATH_CHANGE_FOR_RESOURCE,

    /**
     * Adds the supplied {@link ResourceId} changes from the current transaction to the
     * {@link DataStore}.
     **/
    APPLY_REVERSE_PATH_CHANGE_FOR_RESOURCE,

    /**
     * Adds the supplied {@link ResourceId} changes from the current transaction to the
     * {@link DataStore}.
     **/
    APPLY_CONTENTS_CHANGE_FOR_RESOURCE,

    /**
     * Applies the task entry changes for the supplied {@link ResourceId}
     */
    APPLY_TASK_CHANGES_FOR_RESOURCE_ID,

    /**
     * Cleans up whatever resources were left behind as part of a {@link Path} update during
     * a transaction.
     */
    CLEANUP_RESOURCE_AT_PATH,

    /**
     * Cleans up whatever resources were left behind as part of a {@link ResourceId} update during a transaction.
     */
    CLEANUP_RESOURCE_FOR_RESOURCE_ID,

    /**
     * Cleans up whatever resources were left behind as tasks made for a {@link ResourceId}
     */
    CLEANUP_TASKS_FOR_RESOURCE_ID

}
