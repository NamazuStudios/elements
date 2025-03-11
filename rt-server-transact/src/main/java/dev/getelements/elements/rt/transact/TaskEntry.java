package dev.getelements.elements.rt.transact;

import dev.getelements.elements.rt.exception.NoSuchTaskException;
import dev.getelements.elements.sdk.cluster.id.TaskId;

import java.util.*;

import static java.util.Comparator.comparing;

/**
 * Represents a {@link TaskEntry}
 */
public interface TaskEntry<ScopeT> extends AutoCloseable {

    /**
     * Returns true if the entry represents present entry.
     *
     * @return true if nascent, false otherwise
     */
    default boolean isPresent() {
        return findScope().isPresent();
    }

    /**
     * Returns true if the entry represents an absent entry.
     *
     * @return true if absent, false otherwise
     */
    default boolean isAbsent() {
        return findScope().isEmpty();
    }

    /**
     * Gets the scope of the task entry.
     * @return the scope
     */
    default ScopeT getScope() {
        return findScope().orElseThrow(NoSuchTaskException::new);
    }

    /**
     * Gets the scope of the task entry.
     * @return the scope
     */
    default ScopeT getOriginalScope() {
        return findOriginalScope().orElseThrow(NoSuchTaskException::new);
    }

    /**
     * Finds the scope for the {@link TaskEntry}.
     * @return
     */
    Optional<ScopeT> findScope();

    /**
     * Finds the original scope of the original {@link TaskEntry<ScopeT>}
     * @return
     */
    Optional<ScopeT> findOriginalScope();

    /**
     * Gets the tasks associated with this {@link TaskEntry}.
     *
     * @return the tasks
     */
    Map<TaskId, TransactionalTask> getTasksImmutable();

    /**
     * Gets all of hte original tasks in this {@link TaskEntry}.
     * @return the {@link Set<TransactionalTask>}
     */
    Map<TaskId, TransactionalTask> getOriginalTasksImmutable();

    /**
     * Clears this entry, if the entry is absent, then this has no effect.
     *
     * @return true if the clearing had any effect on this entry.
     */
    boolean delete();

    /**
     * Deletes a {@link TaskId} and timestamp from this {@link TaskIndex}.
     *
     * @param taskId the {@link TaskId}
     */
    boolean deleteTask(final TaskId taskId);

    /**
     * Adds a {@link TaskId} and timestamp to the {@link TaskIndex}.
     *
     * @param taskId the {@link TaskId}
     * @param timestamp the timestamp at which to fire the task
     */
    boolean addTask(final TaskId taskId, long timestamp);

    /**
     * Flushes this {@link TaskEntry} to disk.
     *
     * @param journalEntry
     */
    void flush(final TransactionJournal.MutableEntry journalEntry);

    /**
     * Closes this {@link TaskEntry}.
     */
    void close();

    /**
     * An operational strategy, which defines a list of tasks.
     */
    interface OperationalStrategy<ScopeT> {

        /**
         * Finds the scope of the task entry.
         *
         * @param taskEntry
         * @return
         */
        default Optional<ScopeT> doFindScope(TaskEntry<ScopeT> taskEntry) {
            return taskEntry.findOriginalScope();
        }

        /**
         * Gets the {@link TransactionalTask}s associated with this entry.
         * @param taskEntry
         * @return
         */
        default Map<TaskId, TransactionalTask> doGetTasksImmutable(TaskEntry<ScopeT> taskEntry) {
            return taskEntry.getOriginalTasksImmutable();
        }

        /**
         * Implements the add task operation.
         *
         * @param taskEntry the task entry
         * @param taskId the task ID
         * @param timestamp the timestamp
         */
        default boolean doAddTask(final TaskEntry<ScopeT> taskEntry, final TaskId taskId, final long timestamp) {
            return false;
        }

        /**
         * Deletes the task with the supplied {@link TaskId}.
         *
         * @param taskEntry the task entry
         * @param taskId the task ID
         */
        default boolean doDeleteTask(TaskEntry<ScopeT> taskEntry, TaskId taskId) {
            return false;
        }

        /**
         * Clears the {@link TaskEntry<ScopeT>}
         *
         * @param taskEntry
         * @return
         */
        default boolean doDelete(TaskEntry<ScopeT> taskEntry) {
            return false;
        }

    }

    /**
     * Useful for cerating a {@link Set<TransactionalTask>}.
     *
     * @return the {@link TransactionalTask} set
     */
    static SortedSet<TransactionalTask> newTaskSet() {
        return new TreeSet<>(comparing(TransactionalTask::getTaskId));
    }

}
