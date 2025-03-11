package dev.getelements.elements.rt.exception;

import dev.getelements.elements.rt.ResponseCode;

import static dev.getelements.elements.rt.ResponseCode.OPERATION_NOT_FOUND;

/**
 * Thrown when an operation is not found in the manifest somewhere.
 */
public class OperationNotFoundException extends BaseException {

    public OperationNotFoundException() {}

    public OperationNotFoundException(String message) {
        super(message);
    }

    public OperationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public OperationNotFoundException(Throwable cause) {
        super(cause);
    }

    public OperationNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return OPERATION_NOT_FOUND;
    }

}
