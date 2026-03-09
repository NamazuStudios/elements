package dev.getelements.elements.sdk.model.exception;

/**
 * Thrown when a requested resource cannot be found.
 */
public class NotFoundException extends BaseException {

    /** Creates a new instance. */
    public NotFoundException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public NotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public NotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.NOT_FOUND;
    }

}
