package dev.getelements.elements.rt.exception;

import dev.getelements.elements.rt.ResponseCode;

public class ModuleNotFoundException extends BaseException {

    public ModuleNotFoundException() { }

    public ModuleNotFoundException(String message) {
        super(message);
    }

    public ModuleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModuleNotFoundException(Throwable cause) {
        super(cause);
    }

    public ModuleNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.MODULE_NOT_FOUND;
    }

}
