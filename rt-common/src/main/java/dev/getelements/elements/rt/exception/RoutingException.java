package dev.getelements.elements.rt.exception;

import dev.getelements.elements.rt.ResponseCode;

public class RoutingException extends BaseException {

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

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.ROUTING_EXCEPTION;
    }

}
