package com.namazustudios.socialengine.fts;

/**
 * Created by patricktwohig on 5/16/15.
 */
public class MultipleResultException extends SearchException {

    public MultipleResultException() {
    }

    public MultipleResultException(String message) {
        super(message);
    }

    public MultipleResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleResultException(Throwable cause) {
        super(cause);
    }

    public MultipleResultException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
