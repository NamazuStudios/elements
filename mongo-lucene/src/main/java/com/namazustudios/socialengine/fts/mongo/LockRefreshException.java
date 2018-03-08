package com.namazustudios.socialengine.fts.mongo;

public class LockRefreshException extends RuntimeException {

    public LockRefreshException() { }

    public LockRefreshException(String message) {
        super(message);
    }

    public LockRefreshException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockRefreshException(Throwable cause) {
        super(cause);
    }

    public LockRefreshException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
