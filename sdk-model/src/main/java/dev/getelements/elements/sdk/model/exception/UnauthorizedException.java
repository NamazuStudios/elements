package dev.getelements.elements.sdk.model.exception;

import static dev.getelements.elements.sdk.model.exception.ErrorCode.UNAUTHORIZED;

/**
 * Thrown when access to a resource is unauthorized.
 */
public class UnauthorizedException extends BaseException {

    /** Creates a new instance. */
    public UnauthorizedException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public UnauthorizedException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public UnauthorizedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ErrorCode getCode() {
        return UNAUTHORIZED;
    }

}
