package com.namazustudios.socialengine.rt;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
    void resumeTaskAfterDelay(ResourceId resourceId, long time, TimeUnit timeUnit, TaskId taskId);

    /**
     * Resumes a task that was waiting on a network call.
     *
     * @param resourceId the {@link ResourceId} which owns the task
     * @param taskId the unique {@link TaskId} associated with the network
     * @param result the result of the network operation, passed to the task
     * @return returns {@link Future<Void>} which can be used to determine when the dispatch has been completed.
     */
    void resumeFromNetwork(final ResourceId resourceId, final TaskId taskId, final Object result);

    /**
     * Resumes a task that was waiting for any reason.  This is used to hand an error to the running task in order to a
     * task waiting on an operation.
     *
     * @param resourceId the {@link ResourceId} which owns the task
     * @param taskId the unique {@link TaskId} associated with the network
     * @param throwable the error in the blocked operation
     * @return returns {@link Future<Void>} which can be used to determine when the dispatch has been completed.
     */
    void resumeWithError(final ResourceId resourceId, final TaskId taskId, final Throwable throwable);

}
