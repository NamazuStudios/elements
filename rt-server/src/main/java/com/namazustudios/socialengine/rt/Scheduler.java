package com.namazustudios.socialengine.rt;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
@Proxyable
public interface Scheduler {
    
    /**
     * Performs an action against the resource with the provided {@link ResourceId}.
     *
     * @param resourceId the id of the resource
     * @param operation the operation to perform
     *
     * @return {@link Future<T>} which can be used to monitor the status of the request
     */
    <T> Future<T> perform(ResourceId resourceId, Function<Resource, T> operation);

    /**
     * Performs an action against the resource with the provided ID.
     *
     * @param resourceId the id of the resource
     * @param operation the operation to perform
     *
     * @return {@link Future<Void>} which can be used to monitor the status of the request
     *
     * */
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
     * @return {@link Future<T>} which can be used to monitor the status of the request
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
     * @return {@link Future<Void>} which can be used to monitor the status of the request
     */
    default Future<Void> performV(final Path path, final Consumer<Resource> operation) {
        return perform(path, resource -> {
            operation.accept(resource);
            return null;
        });
    }

    /**
     * Performs the supplied {@link Function<Resource, T>} after a specified delay.  The delay is calculated using the
     * supplied time and {@link TimeUnit}.
     *
     * @param resourceId the id of the resource
     * @param time the time value to delay
     * @param timeUnit the units of the time value
     * @param operation the operation to perform
     *
     * @param <T>
     *
     * @return {@link Future<T>} which can be used to monitor the status of the request
     */
    <T> Future<T> performAfterDelay(ResourceId resourceId, long time, TimeUnit timeUnit, Function<Resource, T> operation);

    /**
     * Invoke {@link #performAfterDelay(ResourceId, long, TimeUnit, Function)} with a {@link Consumer<Resource>}
     * instead of a {@link Function<Resource, ?>}.
     *
     * @param resourceId the id of the resource
     * @param time the time value to delay
     * @param timeUnit the units of the time value
     * @param operation the operation to perform
     *
     * @return {@link Future<Void>} which can be used to monitor the status of the request
     */
    default Future<Void> performAfterDelayV(final ResourceId resourceId,
                                            final long time,
                                            final TimeUnit timeUnit,
                                            final Consumer<Resource> operation) {
        return performAfterDelay(resourceId, time, timeUnit, resource -> {
            operation.accept(resource);
            return null;
        });
    }

    /**
     * Resumes the task associated with the supplied {@link TaskId}.  This allows for the specification of a delay
     * after a specified period of time.
     *
     * @param resourceId the {@link ResourceId}
     * @param taskId the {@link TaskId} of the task
     *
     * @return {@link Future<Void>} which can be used to monitor the status of the request
     */
    default Future<Void> resumeTask(final ResourceId resourceId, final TaskId taskId) {
        return performV(resourceId, r -> r.resume(taskId));
    }

    /**
     * Resumes the task associated with the supplied {@link TaskId}.  This allows for the specification of a delay
     * after a specified period of time.
     *
     * @param resourceId the {@link ResourceId}
     * @param time the time delay
     * @param timeUnit the {@link TimeUnit} instance designating the time units of measure
     * @param taskId the {@link TaskId} of the task
     *
     * @return {@link Future<Void>} which can be used to monitor the status of the request
     */
    default Future<Void> resumeTaskAfterDelay(final ResourceId resourceId,
                                              final long time,
                                              final TimeUnit timeUnit,
                                              final TaskId taskId) {
        return performAfterDelayV(resourceId, time, timeUnit, r -> r.resume(taskId));
    }

    /**
     * Shuts down the Scheduler.  All resources are removed and then the server is shut down.  Attempting to invoke any
     * the other methods after invoking this will result in an {@link IllegalStateException}.
     *
     * @throws {@link IllegalStateException}
     */
    void shutdown() throws IllegalStateException;

}
