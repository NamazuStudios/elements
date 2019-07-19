package com.namazustudios.socialengine.rt.remote.jeromq;

public class JeroMQRoutingException extends RuntimeException {

    public JeroMQRoutingException() {}

    public JeroMQRoutingException(String message) {
        super(message);
    }

    public JeroMQRoutingException(String message, Throwable cause) {
        super(message, cause);
    }

    public JeroMQRoutingException(Throwable cause) {
        super(cause);
    }

    public JeroMQRoutingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
