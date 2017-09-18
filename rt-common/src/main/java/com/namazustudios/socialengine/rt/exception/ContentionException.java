package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.rt.ResponseCode;

/**
 * Created by patricktwohig on 8/14/17.
 */
public class ContentionException extends BaseException {

    public ContentionException() {}

    public ContentionException(String message) {
        super(message);
    }

    public ContentionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContentionException(Throwable cause) {
        super(cause);
    }

    public ContentionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.TOO_BUSY_FATAL;
    }

}
