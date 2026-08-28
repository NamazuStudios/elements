package dev.getelements.elements.sdk.cluster.remote.exception;

import dev.getelements.elements.sdk.model.exception.InternalException;

/**
 * Thrown when a {@link dev.getelements.elements.sdk.cluster.remote.routing.RoutingStrategy} fails to route an
 * invocation to a remote destination.
 */
public class RoutingException extends InternalException {

    public RoutingException() {}

    public RoutingException(String message) {
        super(message);
    }

    public RoutingException(String message, Throwable cause) {
        super(message, cause);
    }

    public RoutingException(Throwable cause) {
        super(cause);
    }

    public RoutingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
