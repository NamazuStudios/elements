package dev.getelements.elements.rt.exception;

import dev.getelements.elements.rt.ResponseCode;

/**
 * Created by patricktwohig on 8/14/17.
 */
public class MethodNotFoundException extends BaseException {

    public MethodNotFoundException() {
    }

    public MethodNotFoundException(String message) {
        super(message);
    }

    public MethodNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MethodNotFoundException(Throwable cause) {
        super(cause);
    }

    public MethodNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.METHOD_NOT_FOUND;
    }

}
