package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.Proxyable;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;
import com.namazustudios.socialengine.rt.annotation.Serialize;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
     *
     * @param resourceId the {@link ResourceId}
     * @param time the time delay
     * @param timeUnit the {@link TimeUnit} instance designating the time units of measure
     * @param taskId the {@link TaskId} of the task
     *
     */
    @RemotelyInvokable
    void resumeTaskAfterDelay(@Serialize ResourceId resourceId,
                              @Serialize long time,
                              @Serialize TimeUnit timeUnit,
                              @Serialize TaskId taskId);

    /**
     * Resumes a task that was waiting on a network call.
     *
     * @param resourceId the {@link ResourceId} which owns the task
     * @param taskId the unique {@link TaskId} associated with the network
     * @param result the result of the network operation, passed to the task
     *
     */
    @RemotelyInvokable
    void resumeFromNetwork(@Serialize ResourceId resourceId, @Serialize TaskId taskId, @Serialize Object result);

    /**
     * Resumes a task that was waiting for any reason.  This is used to hand an error to the running task in order to a
     * task waiting on an operation.
     *
     * @param resourceId the {@link ResourceId} which owns the task
     * @param taskId the unique {@link TaskId} associated with the network
     * @param throwable the error in the blocked operation
     *
     */
    @RemotelyInvokable
    void resumeWithError(@Serialize ResourceId resourceId, @Serialize TaskId taskId, @Serialize Throwable throwable);

}
