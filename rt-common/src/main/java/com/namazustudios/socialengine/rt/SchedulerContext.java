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

    Future<Void> resumeFromNetwork(final ResourceId resourceId, final TaskId taskId, final Object result);

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
