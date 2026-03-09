package dev.getelements.elements.sdk.model.exception;

import static dev.getelements.elements.sdk.model.exception.ErrorCode.CONFLICT;

/** Thrown when a request conflicts with existing data. */
public class ConflictException extends BaseException {

    /** Creates a new instance. */
    public ConflictException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public ConflictException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public ConflictException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public ConflictException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ErrorCode getCode() {
        return CONFLICT;
    }

}
