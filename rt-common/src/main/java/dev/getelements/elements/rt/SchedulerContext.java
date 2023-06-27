package dev.getelements.elements.rt;

import dev.getelements.elements.rt.annotation.*;
import dev.getelements.elements.rt.id.TaskId;
import dev.getelements.elements.rt.routing.SameNodeIdRoutingStrategy;
import dev.getelements.elements.rt.annotation.Proxyable;
import dev.getelements.elements.rt.annotation.RemotelyInvokable;
import dev.getelements.elements.rt.annotation.Serialize;

import java.util.concurrent.TimeUnit;

import static dev.getelements.elements.rt.annotation.RemoteScope.ELEMENTS_RT_PROTOCOL;
import static dev.getelements.elements.rt.annotation.RemoteScope.WORKER_SCOPE;

/**
 * This is the {@link Proxyable} for scheduling tasks within the cluster.
 */
@Proxyable
@RemoteService(scopes = @RemoteScope(scope = WORKER_SCOPE, protocol = ELEMENTS_RT_PROTOCOL))
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
    @RemotelyInvokable(routing = @Routing(SameNodeIdRoutingStrategy.class))
    void resumeTaskAfterDelay(@ProvidesAddress @Serialize TaskId taskId,
                              @Serialize long time,
                              @Serialize TimeUnit timeUnit);

    /**
     * Resumes the supplied task with the {@link TaskId} supplying multiple results to the destination.
     *
     * @param taskId the {@link TaskId} of the supplied task
     * @param results zero or more results from resuming the task
     */
    @RemotelyInvokable(routing = @Routing(SameNodeIdRoutingStrategy.class))
    void resume(@ProvidesAddress @Serialize TaskId taskId, @Serialize Object ... results);

    /**
     * Resumes a task that was waiting on a network call.
     *  @param taskId the unique {@link TaskId} associated with the network
     * @param result the result of the network operation, passed to the task
     *
     */
    @RemotelyInvokable(routing = @Routing(SameNodeIdRoutingStrategy.class))
    void resumeFromNetwork(@ProvidesAddress @Serialize TaskId taskId, @Serialize Object result);

    /**
     * Resumes a task that was waiting for any reason.  This is used to hand an error to the running task in order to a
     * task waiting on an operation.
     *  @param taskId the unique {@link TaskId} associated with the network
     * @param throwable the error in the blocked operation
     *
     */
    @RemotelyInvokable(routing = @Routing(SameNodeIdRoutingStrategy.class))
    void resumeWithError(@ProvidesAddress @Serialize TaskId taskId, @Serialize Throwable throwable);

}
