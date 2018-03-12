package com.namazustudios.socialengine.fts.mongo;

public class LockClosedException extends IllegalStateException {

    public LockClosedException() {
    }

    public LockClosedException(String s) {
        super(s);
    }

    public LockClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockClosedException(Throwable cause) {
        super(cause);
    }
}
