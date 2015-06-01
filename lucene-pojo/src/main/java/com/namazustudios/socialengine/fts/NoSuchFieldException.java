package com.namazustudios.socialengine.fts;

/**
 * Created by patricktwohig on 5/31/15.
 */
public class NoSuchFieldException extends DocumentException {

    public NoSuchFieldException() {}

    public NoSuchFieldException(String message) {
        super(message);
    }

    public NoSuchFieldException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchFieldException(Throwable cause) {
        super(cause);
    }

    public NoSuchFieldException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
