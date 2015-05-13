package com.namazustudios.socialengine.fts;

/**
 * Created by patricktwohig on 5/13/15.
 */
public class IndexException extends RuntimeException {

    public IndexException() {
    }

    public IndexException(String message) {
        super(message);
    }

    public IndexException(String message, Throwable cause) {
        super(message, cause);
    }

    public IndexException(Throwable cause) {
        super(cause);
    }

    public IndexException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
