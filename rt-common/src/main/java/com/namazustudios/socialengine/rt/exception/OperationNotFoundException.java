package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.rt.ResponseCode;

import static com.namazustudios.socialengine.rt.ResponseCode.OPERATION_NOT_FOUND;

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
