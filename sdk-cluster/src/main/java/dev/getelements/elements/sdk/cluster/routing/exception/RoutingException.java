package dev.getelements.elements.sdk.cluster.routing.exception;

import dev.getelements.elements.sdk.model.exception.InternalException;

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
