package com.namazustudios.socialengine.rt;

import com.google.common.base.Stopwatch;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * The Scheduler is the main entry point dispatching requests and operations to the various {@link Resource} instances
 * contained in the underlying services.  This allows for both immediate and timed dispatches of various operations
 * to {@link Resource} instances and is responsible for coordinating and serializing access to each {@link Resource}.
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
     * @return {@link Future<T>} which can be used to monitor the status of the request
     */
    <T> Future<T> perform(ResourceId resourceId, Function<Resource, T> operation, Consumer<Throwable> failure);

    /**
     * Performs an action against the resource with the provided ID.
     *
     * @param resourceId the id of the resource
     * @param operation the operation to perform
     *
     * @return {@link Future<Void>} which can be used to monitor the status of the request
     *
     * */
    default Future<Void> performV(final ResourceId resourceId,
                                  final Consumer<Resource> operation,
                                  final Consumer<Throwable> failure) {
        return perform(resourceId, resource -> {
            operation.accept(resource);
            return null;
        }, failure);
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
    <T> Future<T> perform(Path path, Function<Resource, T> operation, Consumer<Throwable> failure);

    /**
     * Performs an action against the resource with the provided {@link Path}.
     *
     * @param path the path of the resource
     * @param operation the operation to perform
     *
     * @return {@link Future<Void>} which can be used to monitor the status of the request
     */
    default Future<Void> performV(final Path path,
                                  final Consumer<Resource> operation,
                                  final Consumer<Throwable> failure) {
        return perform(path, resource -> {
            operation.accept(resource);
            return null;
        }, failure);
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
    <T> Future<T> performAfterDelay(ResourceId resourceId,
                                    long time, TimeUnit timeUnit,
                                    Function<Resource, T> operation, Consumer<Throwable> failure);

    /**
     * Invoke {@link #performAfterDelay(ResourceId, long, TimeUnit, Function, Consumer)} with a {@link Consumer<Resource>}
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
                                            final long time, final TimeUnit timeUnit,
                                            final Consumer<Resource> operation, final Consumer<Throwable> failure) {
        return performAfterDelay(resourceId, time, timeUnit, resource -> {
            operation.accept(resource);
            return null;
        }, failure);
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
    default Future<Void> resumeTask(final ResourceId resourceId,
                                    final TaskId taskId,
                                    final Consumer<Throwable> failure) {

        final Stopwatch stopwatch = Stopwatch.createStarted();

        return performV(resourceId, r -> {
            final double mills = stopwatch.elapsed(MILLISECONDS);
            final double secondsPerMills = MILLISECONDS.convert(1, SECONDS);
            r.resumeFromScheduler(taskId, mills == 0 ? 0 : (secondsPerMills / mills));
        }, failure);

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
                                              final long time, final TimeUnit timeUnit,
                                              final TaskId taskId, final Consumer<Throwable> failure) {

        final Stopwatch stopwatch = Stopwatch.createStarted();

        return performAfterDelayV(resourceId, time, timeUnit, r -> {
            final double mills = stopwatch.elapsed(MILLISECONDS);
            final double secondsPerMills = MILLISECONDS.convert(1, SECONDS);
            r.resumeFromScheduler(taskId, mills == 0 ? 0 : (secondsPerMills / mills));
        }, failure);

    }

    /**
     * Shuts down the Scheduler.  All resources are removed and then the server is shut down.  Attempting to invoke any
     * the other methods after invoking this will result in an {@link IllegalStateException}.
     *
     * @throws {@link IllegalStateException}
     */
    void shutdown() throws IllegalStateException;

}
