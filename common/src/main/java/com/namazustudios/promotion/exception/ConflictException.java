package com.namazustudios.promotion.exception;

/**
 * Created by patricktwohig on 3/25/15.
 */
public class ConflictException  extends BaseException {

    public ConflictException() {
    }

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConflictException(Throwable cause) {
        super(cause);
    }

    public ConflictException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public int getStatusCode() {
        return 409;
    }

}
