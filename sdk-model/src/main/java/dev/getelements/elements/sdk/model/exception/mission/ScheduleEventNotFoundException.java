package dev.getelements.elements.sdk.model.exception.mission;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

/** Thrown when a schedule event cannot be found. */
public class ScheduleEventNotFoundException extends NotFoundException {

    /** Creates a new instance. */
    public ScheduleEventNotFoundException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public ScheduleEventNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public ScheduleEventNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public ScheduleEventNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public ScheduleEventNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
