package dev.getelements.elements.rt.exception;

import dev.getelements.elements.rt.ResponseCode;

/**
 * Created by patricktwohig on 8/14/17.
 */
public class DuplicateException extends BaseException {

    public DuplicateException() {}

    public DuplicateException(String message) {
        super(message);
    }

    public DuplicateException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateException(Throwable cause) {
        super(cause);
    }

    public DuplicateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.DUPLICATE_RESOURCE;
    }

}
