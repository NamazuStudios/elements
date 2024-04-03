package dev.getelements.elements.exception.mission;

import dev.getelements.elements.exception.NotFoundException;

public class ScheduleNotFoundException extends NotFoundException {

    public ScheduleNotFoundException() {}

    public ScheduleNotFoundException(String message) {
        super(message);
    }

    public ScheduleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScheduleNotFoundException(Throwable cause) {
        super(cause);
    }

    public ScheduleNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
