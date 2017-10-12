package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.rt.ResponseCode;

public class VerbNotSupportedException extends BaseException {

    public VerbNotSupportedException() {
    }

    public VerbNotSupportedException(String message) {
        super(message);
    }

    public VerbNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public VerbNotSupportedException(Throwable cause) {
        super(cause);
    }

    public VerbNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.VERB_NOT_SUPPORTED;
    }

}
