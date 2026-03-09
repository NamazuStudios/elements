package dev.getelements.elements.sdk.model.exception;

/**
 * Thrown when an internal server error occurs.
 */
public class InternalException extends BaseException {

    /** Creates a new instance. */
    public InternalException() {
    }

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public InternalException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public InternalException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public InternalException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public InternalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.UNKNOWN;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return forceFillInStackTrace();
    }

}
