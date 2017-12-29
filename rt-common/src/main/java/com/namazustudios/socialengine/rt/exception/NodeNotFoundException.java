package com.namazustudios.socialengine.rt.exception;

public class NodeNotFoundException extends InternalException {

    public NodeNotFoundException() { }

    public NodeNotFoundException(String message) {
        super(message);
    }

    public NodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeNotFoundException(Throwable cause) {
        super(cause);
    }

    public NodeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
