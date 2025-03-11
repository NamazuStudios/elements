package dev.getelements.elements.sdk.model.exception.notification;

import dev.getelements.elements.sdk.model.exception.InternalException;

/**
 * Thrown when there is a misconfiguration or missing configuration setting pertaining to notifications.
 */
public class NotificationConfigurationException extends InternalException {

    public NotificationConfigurationException() {}

    public NotificationConfigurationException(String message) {
        super(message);
    }

    public NotificationConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotificationConfigurationException(Throwable cause) {
        super(cause);
    }

    public NotificationConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
