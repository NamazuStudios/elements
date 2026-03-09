package dev.getelements.elements.sdk.model.exception.mission;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

/** Thrown when a schedule cannot be found. */
public class ScheduleNotFoundException extends NotFoundException {

    /** Creates a new instance. */
    public ScheduleNotFoundException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public ScheduleNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public ScheduleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public ScheduleNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public ScheduleNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
