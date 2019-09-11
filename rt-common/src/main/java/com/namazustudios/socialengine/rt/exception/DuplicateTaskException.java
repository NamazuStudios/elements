package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.rt.ResponseCode;
import com.namazustudios.socialengine.rt.TaskId;

import static java.lang.String.format;

public class DuplicateTaskException extends BaseException {

    private final TaskId taskId;

    public DuplicateTaskException(TaskId taskId) {
        super(format("Task with id %s already exists.", taskId));
        this.taskId = taskId;
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.DUPLICATE_TASK;
    }

    public TaskId getTaskId() {
        return taskId;
    }

}
