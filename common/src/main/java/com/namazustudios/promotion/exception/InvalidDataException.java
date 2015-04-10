package com.namazustudios.promotion.exception;

/**
 * Created by patricktwohig on 4/1/15.
 */
public class InvalidDataException extends BaseException {

    private final Object model;

    public InvalidDataException() {
        this.model = null;
    }

    public InvalidDataException(String message) {
        super(message);
        model = null;
    }

    public InvalidDataException(String message, Object model) {
        super(message);
        this.model = model;
    }

    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
        this.model = null;
    }

    public InvalidDataException(String message, Throwable cause, Object model) {
        super(message, cause);
        this.model = null;

    }

    public InvalidDataException(Throwable cause) {
        super(cause);
        this.model = null;
    }

    public InvalidDataException(Throwable cause, Object model) {
        super(cause);
        this.model = model;
    }

    public InvalidDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.model = null;
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.INVALID_DATA;
    }

}
