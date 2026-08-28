package dev.getelements.elements.sdk.transact.exception;

import dev.getelements.elements.sdk.cluster.id.TaskId;
import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class TaskNotFoundException extends NotFoundException {

    public TaskNotFoundException() {}

    public TaskNotFoundException(final TaskId taskId) {
        this("Task not found %s".formatted(taskId));
    }

    public TaskNotFoundException(String message) {
        super(message);
    }

    public TaskNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskNotFoundException(Throwable cause) {
        super(cause);
    }

    public TaskNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
