package dev.getelements.elements.sdk.transact;

import dev.getelements.elements.sdk.cluster.id.TaskId;

/**
 * Represents a task from wihing the transactional data store.
 */
public interface TransactionalTask {

    /**
     * Gets the {@link TaskId}.
     * @return the task id
     */
    TaskId getTaskId();

    /**
     * Gets the timestamp when the task needs to resume
     * @return the timestamp
     */
    long getTimestamp();

}
