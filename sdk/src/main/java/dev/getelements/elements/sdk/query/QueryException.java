package dev.getelements.elements.sdk.query;

import dev.getelements.elements.sdk.exception.SdkException;

public class QueryException extends SdkException {

    public QueryException() {}

    public QueryException(String message) {
        super(message);
    }

    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueryException(Throwable cause) {
        super(cause);
    }

    public QueryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
