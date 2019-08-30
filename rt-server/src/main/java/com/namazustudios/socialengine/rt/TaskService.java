package com.namazustudios.socialengine.rt;

import java.util.function.Consumer;

/**
 * Tracks a listing of {@link TaskId}s and their associated {@link Consumer} instances for tracking the results of
 * the task.
 */
public interface TaskService {

    /**
     * Starts the {@link TaskService} and makes it available to begin accepting tasks.
     */
    void start();

    /**
     * Stops the {@link TaskService} and makes it unavailable to accept tasks.  Andy pending tasks are completed
     * with an exception and all references cleared.
     */
    void stop();

    /**
     * Registers a new {@link TaskId} and set of consumers to handle the results.
     *
     * @param taskId the {@link TaskId}
     * @param consumer the {@link Consumer} for the result.
     * @param throwableTConsumer
     */
    void register(TaskId taskId, Consumer<Object> consumer, Consumer<Throwable> throwableTConsumer);

    /**
     * Finishes a task with the supplied result.  In completing the task, references are immediately cleared
     * and no further messages can be sent to that {@link TaskId}.
     *
     * As not all tasks are registered with a set of listeners, this may simply return false indicating that no
     * listeners were notified.
     *
     * @param taskId the {@link TaskId}
     * @param result the {@link Object} that is the result of completing the task
     */
    boolean finishWithResult(TaskId taskId, Object result);

    /**
     * Fails a task with the supplied {@link Throwable}, indicating an error.  In completing the task, references are
     * immediately cleared and no further messages can be sent to that {@link TaskId}.
     *
     * As not all tasks are registered with a set of listeners, this may simply return false indicating that no
     * listeners were notified.
     *
     * @param taskId the {@link TaskId}
     * @param error the {@link Throwable} that is the result of failing the task
     */
    boolean finishWithError(TaskId taskId, Throwable error);

}
