package com.namazustudios.socialengine.exception;

import static com.namazustudios.socialengine.exception.ErrorCode.CONFLICT;

public class ConflictException extends BaseException {

    public ConflictException() {}

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
    public ErrorCode getCode() {
        return CONFLICT;
    }

}
