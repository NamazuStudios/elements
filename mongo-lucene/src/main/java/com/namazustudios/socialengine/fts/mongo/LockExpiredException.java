package com.namazustudios.socialengine.fts.mongo;

import java.io.IOException;

public class LockExpiredException extends IOException {
    public LockExpiredException() {
    }

    public LockExpiredException(String message) {
        super(message);
    }

    public LockExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public LockExpiredException(Throwable cause) {
        super(cause);
    }
}
