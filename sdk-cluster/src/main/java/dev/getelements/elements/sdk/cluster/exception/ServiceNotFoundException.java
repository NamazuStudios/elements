package dev.getelements.elements.sdk.cluster.ex;

import dev.getelements.elements.rt.ResponseCode;
import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class ServiceNotFoundException extends NotFoundException {

    public ServiceNotFoundException() {
    }

    public ServiceNotFoundException(String message) {
        super(message);
    }

    public ServiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceNotFoundException(Throwable cause) {
        super(cause);
    }

    public ServiceNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.SERVICE_NOT_FOUND;
    }

}
