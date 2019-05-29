package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.ProvidesAddress;
import com.namazustudios.socialengine.rt.annotation.Proxyable;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;
import com.namazustudios.socialengine.rt.annotation.Serialize;
import com.namazustudios.socialengine.rt.remote.AddressedRoutingStrategy;

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
     *  @param time the time delay
     * @param timeUnit the {@link TimeUnit} instance designating the time units of measure
     * @param taskId the {@link TaskId} of the task
     *
     */
    @RemotelyInvokable(AddressedRoutingStrategy.class)
    void resumeTaskAfterDelay(@Serialize long time,
                              @Serialize TimeUnit timeUnit,
                              @ProvidesAddress @Serialize TaskId taskId);

    /**
     * Resumes the supplied task with the {@link TaskId} supplying multiple results to the destination.
     *
     * @param taskId the {@link TaskId} of the supplied task
     * @param results zero or more results from resuming the task
     */
    @RemotelyInvokable(AddressedRoutingStrategy.class)
    void resume(@ProvidesAddress @Serialize TaskId taskId, @Serialize Object ... results);

    /**
     * Resumes a task that was waiting on a network call.
     *  @param taskId the unique {@link TaskId} associated with the network
     * @param result the result of the network operation, passed to the task
     *
     */
    @RemotelyInvokable(AddressedRoutingStrategy.class)
    void resumeFromNetwork(@ProvidesAddress @Serialize TaskId taskId, @Serialize Object result);

    /**
     * Resumes a task that was waiting for any reason.  This is used to hand an error to the running task in order to a
     * task waiting on an operation.
     *  @param taskId the unique {@link TaskId} associated with the network
     * @param throwable the error in the blocked operation
     *
     */
    @RemotelyInvokable(AddressedRoutingStrategy.class)
    void resumeWithError(@ProvidesAddress @Serialize TaskId taskId, @Serialize Throwable throwable);

}
