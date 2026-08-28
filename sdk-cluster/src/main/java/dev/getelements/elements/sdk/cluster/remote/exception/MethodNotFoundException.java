package dev.getelements.elements.sdk.cluster.remote.exception;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

/**
 * Thrown when a remotely-invoked method cannot be located on the target service.
 */
public class MethodNotFoundException extends NotFoundException {

    public MethodNotFoundException() {
    }

    public MethodNotFoundException(String message) {
        super(message);
    }

    public MethodNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MethodNotFoundException(Throwable cause) {
        super(cause);
    }

    public MethodNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
