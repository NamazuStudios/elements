package com.namazustudios.socialengine.exception;

/**
 * Created by patricktwohig on 7/18/17.
 */
public class NotImplementedException extends BaseException {

    public NotImplementedException() {}

    public NotImplementedException(String message) {
        super(message);
    }

    public NotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotImplementedException(Throwable cause) {
        super(cause);
    }

    public NotImplementedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.NOT_IMPLEMENTED;
    }

}
