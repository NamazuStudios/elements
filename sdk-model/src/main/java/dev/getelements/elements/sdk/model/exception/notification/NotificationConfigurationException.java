package dev.getelements.elements.sdk.model.exception.notification;

import dev.getelements.elements.sdk.model.exception.InternalException;

/**
 * Thrown when there is a misconfiguration or missing configuration setting pertaining to notifications.
 */
public class NotificationConfigurationException extends InternalException {

    /** Creates a new instance. */
    public NotificationConfigurationException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public NotificationConfigurationException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public NotificationConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public NotificationConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public NotificationConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
