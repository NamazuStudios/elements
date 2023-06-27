package dev.getelements.elements.rt.exception;

import dev.getelements.elements.rt.ResponseCode;

public class InvalidNodeIdException extends InvalidIdException {

    public InvalidNodeIdException() {}

    public InvalidNodeIdException(String message) {
        super(message);
    }

    public InvalidNodeIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidNodeIdException(Throwable cause) {
        super(cause);
    }

    public InvalidNodeIdException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.INVALID_NODE_ID;
    }

}
