package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.rt.ResponseCode;
import com.namazustudios.socialengine.rt.id.TaskId;

import static java.lang.String.format;

public class TaskKilledException extends BaseException {

    private final TaskId taskId;

    public TaskKilledException(TaskId taskId) {
        super(format("Task with id %s was killed.", taskId));
        this.taskId = taskId;
    }

    public TaskId getTaskId() {
        return taskId;
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.TASK_KILLED;
    }

}
