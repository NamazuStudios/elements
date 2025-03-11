package dev.getelements.elements.rt.exception;

import dev.getelements.elements.rt.ResponseCode;
import dev.getelements.elements.sdk.cluster.id.TaskId;

import static java.lang.String.format;

public class NoSuchTaskException extends BaseException {

    private final TaskId taskId;

    public NoSuchTaskException() {
        super("No such task.");
        this.taskId = null;
    }

    public NoSuchTaskException(final TaskId taskId) {
        super(format("Task with id %s not found.", taskId));
        this.taskId = taskId;
    }

    public TaskId getTaskId() {
        return taskId;
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.TASK_NOT_FOUND;
    }

}
