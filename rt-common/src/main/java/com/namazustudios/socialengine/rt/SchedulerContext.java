package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.Proxyable;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;
import com.namazustudios.socialengine.rt.annotation.Serialize;
import com.namazustudios.socialengine.rt.exception.DuplicateTaskException;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * This is the {@link Proxyable} for scheduling tasks within the cluster.
 */
@Proxyable
public interface SchedulerContext {

    /**
     * Starts this {@link SchedulerContext}.
     */
    default void start() {}

    /**
     * Stops this {@link SchedulerContext}.
     */
    default void stop() {}

    /**
     * Resumes the task associated with the supplied {@link TaskId}.  This allows for the specification of a delay
     * after a specified period of time.
     * @param taskId the {@link TaskId} of the task
     * @param time the time delay
     * @param timeUnit the {@link TimeUnit} instance designating the time units of measure
     *
     */
    @RemotelyInvokable
    void resumeTaskAfterDelay(@Serialize TaskId taskId,
                              @Serialize long time,
                              @Serialize TimeUnit timeUnit);

    /**
     * Resumes the supplied task with the {@link TaskId} supplying multiple results to the destination.
     *
     * @param taskId the {@link TaskId} of the supplied task
     * @param results zero or more results from resuming the task
     */
    @RemotelyInvokable
    void resume(@Serialize TaskId taskId, @Serialize Object ... results);

    /**
     * Resumes a task that was waiting on a network call.
     *  @param taskId the unique {@link TaskId} associated with the network
     * @param result the result of the network operation, passed to the task
     *
     */
    @RemotelyInvokable
    void resumeFromNetwork(@Serialize TaskId taskId, @Serialize Object result);

    /**
     * Resumes a task that was waiting for any reason.  This is used to hand an error to the running task in order to a
     * task waiting on an operation.
     *  @param taskId the unique {@link TaskId} associated with the network
     * @param throwable the error in the blocked operation
     *
     */
    @RemotelyInvokable
    void resumeWithError(@Serialize TaskId taskId, @Serialize Throwable throwable);

    /**
     * Registers the supplied {@link TaskId}, result {@link Consumer}, and {@link Consumer<Throwable>} for the
     * associated task.  The {@link SchedulerContext} will store the associated result consumers internally and at
     * some point later invoke either the result or failure consumer.
     *
     * However, it is advisable to account for scenarios where neither consumer may be called.  This may happen in
     * the event the remote service has been restarted and in-memory consumers were lost.  The calling code should
     * have a strategy to account for timeouts where appropriate.
     *
     * @param taskId the task ID
     * @param tConsumer
     * @param throwableTConsumer
     * @param <T>
     * @throws DuplicateTaskException if the supplied {@link TaskId} was already registered.
     */
    @RemotelyInvokable
    <T> void register(@Serialize TaskId taskId, Consumer<T> tConsumer, Consumer<Throwable> throwableTConsumer);

    /**
     * Finishes the task with associated {@link TaskId} with the provided result.  Consumers previously registered with
     * the {@link #register(TaskId, Consumer, Consumer)} call will be processed.
     *
     * @param taskId
     * @param <T>
     */
    @RemotelyInvokable
    <T> void finishWithResult(@Serialize TaskId taskId, T result);

    /**
     * Finishes the task with associated {@link TaskId} with the provided {@link Throwable} error.
     *
     * @param taskId the {@link TaskId}
     */
    @RemotelyInvokable
    void finishWithError(@Serialize TaskId taskId, Throwable error);

}
