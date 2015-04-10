package com.namazustudios.promotion.exception;

/**
 * Created by patricktwohig on 3/25/15.
 */
public abstract  class BaseException extends RuntimeException {

    public BaseException() {
    }

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

    /**
     * Gets the error code.
     *
     * @return
     */
    public abstract ErrorCode getCode();

}
