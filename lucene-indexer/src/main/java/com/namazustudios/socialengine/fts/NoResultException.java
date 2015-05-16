package com.namazustudios.socialengine.fts;

/**
 * Created by patricktwohig on 5/16/15.
 */
public class NoResultException extends SearchException {

    public NoResultException() {}

    public NoResultException(String message) {
        super(message);
    }

    public NoResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoResultException(Throwable cause) {
        super(cause);
    }

    public NoResultException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
