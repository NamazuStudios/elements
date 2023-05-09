package dev.getelements.elements.rt.exception;

import dev.getelements.elements.rt.ResponseCode;

public class UnsupportedMediaTypeException extends BaseException {

    public UnsupportedMediaTypeException() {}

    public UnsupportedMediaTypeException(String message) {
        super(message);
    }

    public UnsupportedMediaTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedMediaTypeException(Throwable cause) {
        super(cause);
    }

    public UnsupportedMediaTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.UNSUPPORTED_MEDIA_TYPE;
    }

}
