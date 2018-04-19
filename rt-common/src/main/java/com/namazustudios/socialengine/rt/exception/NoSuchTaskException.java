package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.rt.TaskId;

import static java.lang.String.format;

public class NoSuchTaskException extends RuntimeException {

    private final TaskId taskId;

    public NoSuchTaskException(final TaskId taskId) {
        super(format("Task with id %s not found.", taskId));
        this.taskId = taskId;
    }

    public TaskId getTaskId() {
        return taskId;
    }

}
