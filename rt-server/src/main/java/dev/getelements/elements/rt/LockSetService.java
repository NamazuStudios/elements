package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.sdk.util.Monitor;

import java.util.Collection;
import java.util.SortedSet;

/**
 * Locks a set of objects based on their identity in a consistent and order ensuring no deadlocks when having to acquire
 * multiple locks.
 */
public interface LockSetService {

    /**
     * Logs the status of the {@link LockSetService}.
     */
    void logStatus();

    /**
     * Gets a {@link Monitor} which will lock all resources in the {@link Path} instances for read operations.
     *
     * @param paths a collection of {@link Path} instances
     * @return an instance of {@link Monitor} which aggregates locks across all requested resources.
     */
    default Monitor getPathReadMonitor(final Collection<Path> paths) {

        var monitor = Monitor.empty();

        try {

            for (final var path : paths) {
                monitor = monitor.then(getPathReadMonitor(path));
            }

            return monitor;
        } catch (Throwable th) {
            monitor.close();
            throw th;
        }

    }

    /**
     * Gets a {@link Monitor} which will lock all resources in the {@link Path} instances for write operations.
     *
     * @param paths a collection of {@link Path} instances
     * @return an instance of {@link Monitor} which aggregates locks across all requested resources.
     */
    default Monitor getPathWriteMonitor(final Collection<Path> paths) {

        var monitor = Monitor.empty();

        try {

            for (final var path : paths) {
                monitor = monitor.then(getPathWriteMonitor(path));
            }

            return monitor;
        } catch (Throwable th) {
            monitor.close();
            throw th;
        }

    }

    /**
     * Gets a single {@link Monitor} for the supplied {@link ResourceId} (for reading). When requesting a {@link Path}
     * with a {@link Path#WILDCARD} or {@link Path#WILDCARD_RECURSIVE} component, this will essentially acquire all
     * monitors for matching paths.
     *
     * @param path the {@link Path}
     * @return the {@link Monitor} locking those {@link Path} instances
     */
    Monitor getPathReadMonitor(Path path);

    /**
     * Gets a single {@link Monitor} for the supplied {@link ResourceId} (for writing). When requesting a {@link Path}
     * with a {@link Path#WILDCARD} or {@link Path#WILDCARD_RECURSIVE} component, this will essentially acquire all
     * monitors for matching paths.
     *
     * @param path the {@link Path}
     * @return the {@link Monitor} locking those {@link Path} instances
     */
    Monitor getPathWriteMonitor(Path path);

    /**
     * Gets a {@link Monitor} which will lock all resources in the {@link ResourceId} instances for read operations.
     *
     * @param resourceIds a collection of {@link ResourceId} instances
     * @return an instance of {@link Monitor} which aggregates locks across all requested resources.
     */
    default Monitor getResourceIdReadMonitor(final SortedSet<ResourceId> resourceIds) {

        var monitor = Monitor.empty();

        try {

            for (final var resourceId : resourceIds) {
                monitor = monitor.then(getResourceIdReadMonitor(resourceId));
            }

            return monitor;
        } catch (Throwable th) {
            monitor.close();
            throw th;
        }

    }

    /**
     * Gets a {@link Monitor} which will lock all resources in the {@link ResourceId} instances for write operations.
     *
     * @param resourceIds a collection of {@link ResourceId} instances
     * @return an instance of {@link Monitor} which aggregates locks across all requested resources.
     */
    default Monitor getResourceIdWriteMonitor(final SortedSet<ResourceId> resourceIds) {

        var monitor = Monitor.empty();

        try {

            for (final var resourceId : resourceIds) {
                monitor = monitor.then(getResourceIdWriteMonitor(resourceId));
            }

            return monitor;
        } catch (Throwable th) {
            monitor.close();
            throw th;
        }

    }

    /**
     * Gets a single {@link Monitor} for the supplied {@link ResourceId} (for reading).
     *
     * @param resourceId the {@link ResourceId}
     * @return the {@link Monitor} locking those {@link ResourceId} instances
     */
    Monitor getResourceIdReadMonitor(ResourceId resourceId);

    /**
     * Gets a single {@link Monitor} for the supplied {@link ResourceId} (for writing).
     *
     * @param resourceId the {@link ResourceId}
     * @return the {@link Monitor} locking those {@link ResourceId} instances
     */
    Monitor getResourceIdWriteMonitor(ResourceId resourceId);

}
