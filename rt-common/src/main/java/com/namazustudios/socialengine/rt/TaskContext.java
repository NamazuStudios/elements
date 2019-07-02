package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.*;
import com.namazustudios.socialengine.rt.exception.DuplicateTaskException;

import java.util.function.Consumer;

/**
 * Manages the global state of currently operating tasks.  This allows a callers to listen for tasks as they are running
 * and receive callbacks that the task has completed.  While each {@link Resource} is responsible for managing tasks,
 * this is the hub through which all tasks are managed.
 */
@Proxyable
public interface TaskContext {

    /**
     * Starts this {@link TaskContext}.
     */
    default void start() {}

    /**
     * Stops this {@link TaskContext}.
     */
    default void stop() {}

    /**
     * Registers the supplied {@link TaskId}, result {@link Consumer}, and {@link Consumer<Throwable>} for the
     * associated task.  The {@link TaskContext} will store the associated result consumers internally and at
     * some point later invoke either the result or failure consumer.
     *
     * However, it is advisable to account for scenarios where neither consumer may be called.  This may happen in
     * the event the remote service has been restarted and in-memory consumers were lost.  The calling code should
     * have a strategy to account for timeouts where appropriate.
     *
     * @param taskId the task ID
     * @param tConsumer
     * @param throwableTConsumer
     * @throws DuplicateTaskException if the supplied {@link TaskId} was already registered.
     */
    @RemotelyInvokable
    void register(@Serialize TaskId taskId,
                      @ResultHandler Consumer<Object> tConsumer,
                      @ErrorHandler Consumer<Throwable> throwableTConsumer);

    /**
     * Finishes the task with associated {@link TaskId} with the provided result.  Consumers previously registered with
     * the {@link #register(TaskId, Consumer, Consumer)} call will be processed.
     *
     * @param taskId
     */
    @RemotelyInvokable
    void finishWithResult(@Serialize TaskId taskId, Object result);

    /**
     * Finishes the task with associated {@link TaskId} with the provided {@link Throwable} error.
     *
     * @param taskId the {@link TaskId}
     */
    @RemotelyInvokable
    void finishWithError(@Serialize TaskId taskId, Throwable error);

}
