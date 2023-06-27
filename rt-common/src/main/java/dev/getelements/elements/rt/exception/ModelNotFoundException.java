package dev.getelements.elements.rt.exception;

import dev.getelements.elements.rt.ResponseCode;

public class ModelNotFoundException extends ServiceNotFoundException {

    public ModelNotFoundException() {}

    public ModelNotFoundException(String message) {
        super(message);
    }

    public ModelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelNotFoundException(Throwable cause) {
        super(cause);
    }

    public ModelNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.MODEL_NOT_FOUND;
    }

}
