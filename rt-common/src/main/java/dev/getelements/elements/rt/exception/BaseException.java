package dev.getelements.elements.rt.exception;

import dev.getelements.elements.rt.ResponseCode;

/**
 * Created by patricktwohig on 8/14/17.
 */
public abstract class BaseException extends RuntimeException {

    public BaseException() {}

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(Throwable cause) {
        super(cause);
    }

    public BaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public abstract ResponseCode getResponseCode();

}
