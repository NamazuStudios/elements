package com.namazustudios.socialengine.rt.remote;

public class RemoteInvocationException extends RuntimeException {

    public RemoteInvocationException() {
    }

    public RemoteInvocationException(String message) {
        super(message);
    }

    public RemoteInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteInvocationException(Throwable cause) {
        super(cause);
    }

    public RemoteInvocationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
