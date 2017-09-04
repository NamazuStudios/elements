package com.namazustudios.socialengine.rt;

import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The Container is the hub of communications between the outside world and the resources.   It is responsible
 * for dispatching and serving requests as well as managing flow and access to the resources.  It must ensure
 * that access to resources are performed with thread-safety and concurrency in mind.
 *
 * Created by patricktwohig on 8/22/15.
 */
public interface Container {

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
     * Shuts down the Container.  All resources are removed and then the server is shut down.  Attempting to invoke any
     * the other methods after invoking this will result in an {@link IllegalStateException}.
     *
     * @throws {@link IllegalStateException}
     */
    void shutdown() throws IllegalStateException;

}
