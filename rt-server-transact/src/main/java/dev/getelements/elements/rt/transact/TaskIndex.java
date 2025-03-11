package dev.getelements.elements.rt.transact;

import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.TaskEntry.OperationalStrategy;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Indexes all of the {@link TransactionalTask} instances in the datastore.
 */
public interface TaskIndex {

    /**
     * lists all tasks in the {@link TransactionalTask}. The returned {@link Stream} should be closed when it is
     * finished as it may consume IO resources.
     *
     * @return a listing of {@link TransactionalTask}
     */
    default Stream<TransactionalTask> listAllTasks() {

        final var strategy = new OperationalStrategy<ResourceId>(){};

        return listAllEntriesByResource().flatMap(ctor -> {
            try (var entry = ctor.apply(strategy)) {
                final var list = new ArrayList<>(entry.getTasksImmutable().values());
                return list.stream();
            }
        });

    }

    /**
     * Gets all entries in the {@link TaskIndex}.
     * @return all entries
     */
    Stream<Function<OperationalStrategy<ResourceId>, TaskEntry<ResourceId>>> listAllEntriesByResource();

    /**
     * Finds the {@link TaskEntry<ResourceId>} for the supplied {@link ResourceId}.
     *
     * @param ctor the constructor fo the operational strategy
     * @param resourceId          the {@link ResourceId}
     * @return the {@link TaskEntry<ResourceId>}
     */
    Optional<TaskEntry<ResourceId>> findTaskEntry(
            Supplier<OperationalStrategy<ResourceId>> ctor,
            ResourceId resourceId);

    /**
     * Gets the {@link TaskEntry<ResourceId>} for the supplied {@link ResourceId}.
     *
     * @param ctor the constructor fo the operational strategy
     * @param resourceId          the {@link ResourceId}
     * @return the {@link TaskEntry<ResourceId>}
     */
    TaskEntry<ResourceId> getOrCreateTaskEntry(
            Supplier<OperationalStrategy<ResourceId>> ctor,
            ResourceId resourceId);

    /**
     * Cleans up the task changes with the supplied {@link ResourceId} and transaction id.
     * @param resourceId
     * @param transactionId
     */
    void cleanup(ResourceId resourceId, String transactionId);

    /**
     * Applies task changes with the supplied {@link ResourceId} and transaction id.
     * @param resourceId
     * @param transactionId
     */
    void applyChange(ResourceId resourceId, String transactionId);

}
