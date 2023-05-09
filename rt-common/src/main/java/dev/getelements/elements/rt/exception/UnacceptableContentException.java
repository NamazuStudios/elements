package dev.getelements.elements.rt.exception;

import dev.getelements.elements.rt.ResponseCode;

public class UnacceptableContentException extends BaseException {

    public UnacceptableContentException() {
    }

    public UnacceptableContentException(String message) {
        super(message);
    }

    public UnacceptableContentException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnacceptableContentException(Throwable cause) {
        super(cause);
    }

    public UnacceptableContentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.UNACCEPTABLE_CONTENT;
    }

}
