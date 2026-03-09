package dev.getelements.elements.sdk.model.exception;

/**
 * Thrown when the service is overloaded and cannot process the request.
 */
public class TooBusyException extends BaseException {

    /** Creates a new instance. */
    public TooBusyException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public TooBusyException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public TooBusyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public TooBusyException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public TooBusyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.OVERLOAD;
    }

}
