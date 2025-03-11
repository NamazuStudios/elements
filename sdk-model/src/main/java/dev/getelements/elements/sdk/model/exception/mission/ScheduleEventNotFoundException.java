package dev.getelements.elements.sdk.model.exception.mission;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class ScheduleEventNotFoundException extends NotFoundException {

    public ScheduleEventNotFoundException() {}

    public ScheduleEventNotFoundException(String message) {
        super(message);
    }

    public ScheduleEventNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScheduleEventNotFoundException(Throwable cause) {
        super(cause);
    }

    public ScheduleEventNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
