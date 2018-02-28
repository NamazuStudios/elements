package com.namazustudios.socialengine.exception;

public class NoSessionException extends ForbiddenException {
    public NoSessionException() {
    }

    public NoSessionException(String message) {
        super(message);
    }

    public NoSessionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSessionException(Throwable cause) {
        super(cause);
    }

    public NoSessionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
