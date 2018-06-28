package com.namazustudios.socialengine.rt.exception;

public class ResourcePersistenceException extends InternalException {

    public ResourcePersistenceException() {}

    public ResourcePersistenceException(String message) {
        super(message);
    }

    public ResourcePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourcePersistenceException(Throwable cause) {
        super(cause);
    }

    public ResourcePersistenceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
