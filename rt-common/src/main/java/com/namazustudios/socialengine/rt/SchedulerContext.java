package com.namazustudios.socialengine.rt;

import com.google.common.base.Stopwatch;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This is the {@link Proxyable} for scheduling tasks within the cluster.
 */
@Proxyable
public interface SchedulerContext {

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
    Future<Void> resumeTaskAfterDelay(ResourceId resourceId, long time, TimeUnit timeUnit, TaskId taskId);

    /**
     * Resumes a task that was waiting on a network call.
     *
     * @param resourceId the {@link ResourceId} which owns the task
     * @param taskId the unique {@link TaskId} associated with the network
     * @param result the result of the network operation, passed to the task
     * @return returns {@link Future<Void>} which can be used to determine when the dispatch has been completed.
     */
    Future<Void> resumeFromNetwork(final ResourceId resourceId, final TaskId taskId, final Object result);

    /**
     * Resumes a task that was waiting for any reason.  This is used to hand an error to the running task in order to a
     * task waiting on an operation.
     *
     * @param resourceId the {@link ResourceId} which owns the task
     * @param taskId the unique {@link TaskId} associated with the network
     * @param throwable the error in the blocked operation
     * @return returns {@link Future<Void>} which can be used to determine when the dispatch has been completed.
     */
    Future<Void> resumeWithError(final ResourceId resourceId, final TaskId taskId, final Throwable throwable);

    /**
     * Performs an action against the resource with the provided {@link Path}.  Note that the provided
     * {@link Resource} may be a proxy for a remote {@link Resource}.  Once the provided {@link Function <Resource, T>}
     * returns, the {@link Resource} may no-longer be valid.
     *
     * The {@link ResourceContext} may background the processing of the operation.
     *
     * @param path the path of the resource
     * @param operation the operation to perform
     *
     * @param <T>
     */
    <T> Future<T> perform(Path path, Function<Resource, T> operation);

    /**
     * Performs an action against the resource with the provided {@link Path}.  The default implementation simply passes
     * an {@link Function<Resource, Void>} to {@link #perform(Path, Function)}.
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

}
