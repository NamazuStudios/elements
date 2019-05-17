package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.rt.ResponseCode;

/**
 * Used when a remote procedure call relays an instance of {@link Throwable}.
 */
public class RemoteThrowableException extends BaseException {

    public RemoteThrowableException() {}

    public RemoteThrowableException(String message) {
        super(message);
    }

    public RemoteThrowableException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteThrowableException(Throwable cause) {
        super(cause);
    }

    public RemoteThrowableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.INTERNAL_ERROR_FATAL;
    }

}
