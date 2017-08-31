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
public interface Container<ResourceT extends Resource> {

    /**
     * Performs an action against the resource with the provided {@link ResourceId}.
     *
     * @param resourceId the id of the resource
     * @param operation the operation to perform
     *
     * @param <T>
     */
    <T> Future<T> perform(final ResourceId resourceId, Function<ResourceT, T> operation);

    /**
     * Performs an action against the resource with the provided ID.
     *
     * @param resourceId the id of the resource
     * @param operation the operation to perform
     *
     */
    default Future<Void> performV(final ResourceId resourceId, final Consumer<ResourceT> operation) {
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
    <T> Future<T> perform(final Path path, Function<ResourceT, T> operation);

    /**
     * Performs an action against the resource with the provided {@link Path}.
     *
     * @param path the path of the resource
     * @param operation the operation to perform
     *
     */
    default Future<Void> performV(final Path path, Consumer<ResourceT> operation) {
        return perform(path, resource -> {
            operation.accept(resource);
            return null;
        });
    }

    /**
     * Shuts down the server.  All resources are removed and then the server is shut down.  Attempting
     * to invoke any of hte other methods will result in an exception.
     *
     * @throws {@link IllegalStateException}
     */
    void shutdown();

}
