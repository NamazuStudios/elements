package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.rt.ResponseCode;

import static com.namazustudios.socialengine.rt.ResponseCode.HANDLER_TIMEOUT;

public class HandlerTimeoutException extends BaseException {

    public HandlerTimeoutException() {}

    public HandlerTimeoutException(String message) {
        super(message);
    }

    public HandlerTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public HandlerTimeoutException(Throwable cause) {
        super(cause);
    }

    public HandlerTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return HANDLER_TIMEOUT;
    }

}
