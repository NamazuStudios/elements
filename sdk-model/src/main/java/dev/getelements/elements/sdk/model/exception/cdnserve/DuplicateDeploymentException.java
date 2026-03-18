package dev.getelements.elements.sdk.model.exception.cdnserve;

import dev.getelements.elements.sdk.model.exception.DuplicateException;

/** Thrown when a duplicate CDN deployment is detected. */
public class DuplicateDeploymentException extends DuplicateException {

    /** Creates a new instance. */
    public DuplicateDeploymentException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public DuplicateDeploymentException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public DuplicateDeploymentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public DuplicateDeploymentException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public DuplicateDeploymentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
