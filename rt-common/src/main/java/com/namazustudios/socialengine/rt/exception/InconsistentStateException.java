package com.namazustudios.socialengine.rt.exception;

public class InconsistentStateException extends InternalException {

    public InconsistentStateException() {}

    public InconsistentStateException(String message) {
        super(message);
    }

    public InconsistentStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public InconsistentStateException(Throwable cause) {
        super(cause);
    }

    public InconsistentStateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
