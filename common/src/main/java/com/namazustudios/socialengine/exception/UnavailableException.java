package com.namazustudios.socialengine.exception;

public class UnavailableException extends InternalException {

    public UnavailableException() {}

    public UnavailableException(String message) {
        super(message);
    }

    public UnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnavailableException(Throwable cause) {
        super(cause);
    }

    public UnavailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
