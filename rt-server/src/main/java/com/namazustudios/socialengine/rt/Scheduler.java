package com.namazustudios.socialengine.rt;

import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The Scheduler is the main entry point dispatching requests and operations to the various {@link Resource} instances
 * contained in the underlying services.  This allows for both immediate and timed dispatches of various operations
 * to {@link Resource} instances and is responsible for coordinating and serializing access through the {@link Lock}
 * instances obtained via the {@link PathLockFactory}.
 *
 * Created by patricktwohig on 8/22/15.
 */
public interface Scheduler {
    
    /**
     * Performs an action against the resource with the provided {@link ResourceId}.
     *
     * @param resourceId the id of the resource
     * @param operation the operation to perform
     *
     * @param <T>
     */
    <T> Future<T> perform(ResourceId resourceId, Function<Resource, T> operation);

    /**
     * Performs an action against the resource with the provided ID.
     *
     * @param resourceId the id of the resource
     * @param operation the operation to perform
     *
     */
    default Future<Void> performV(final ResourceId resourceId, final Consumer<Resource> operation) {
        return perform(resourceId, resource -> {
            operation.accept(resource);
            return null;
        });
    }

    /**
     * Performs an action against the resource with the provided {@link ResourceId}
     *
     * @param path the path of the resource
     * @param operation the operation to perform
     *
     * @param <T>
     */
    <T> Future<T> perform(Path path, Function<Resource, T> operation);

    /**
     * Performs an action against the resource with the provided {@link Path}.
     *
     * @param path the path of the resource
     * @param operation the operation to perform
     *
     */
    default Future<Void> performV(final Path path, final Consumer<Resource> operation) {
        return perform(path, resource -> {
            operation.accept(resource);
            return null;
        });
    }

    /**
     * Shuts down the Scheduler.  All resources are removed and then the server is shut down.  Attempting to invoke any
     * the other methods after invoking this will result in an {@link IllegalStateException}.
     *
     * @throws {@link IllegalStateException}
     */
    void shutdown() throws IllegalStateException;

}
