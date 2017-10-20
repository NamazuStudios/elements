package com.namazustudios.socialengine.rt;

import com.google.common.base.Stopwatch;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * This is the {@link Proxyable} interface to the scheduler and related functions.
 */
@Proxyable
public interface SchedulerContext {

    /**
     * Resumes the task associated with the supplied {@link TaskId}.  This allows for the specification of a delay
     * after a specified period of time.
     *
     * @param resourceId the {@link ResourceId}
     * @param taskId the {@link TaskId} of the task
     *
     * @return {@link Future <Void>} which can be used to monitor the status of the request
     */
    default Future<Void> resumeTask(final ResourceId resourceId, final TaskId taskId) {
        return resumeTaskAfterDelay(resourceId, 0, TimeUnit.SECONDS, taskId);
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
    Future<Void> resumeTaskAfterDelay(ResourceId resourceId, long time, TimeUnit timeUnit, TaskId taskId);

}
