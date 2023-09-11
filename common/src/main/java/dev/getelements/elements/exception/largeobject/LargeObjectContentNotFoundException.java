package dev.getelements.elements.exception.largeobject;

import dev.getelements.elements.exception.NotFoundException;

public class LargeObjectContentNotFoundException extends NotFoundException {


    public LargeObjectContentNotFoundException() {}

    public LargeObjectContentNotFoundException(String message) {
        super(message);
    }

    public LargeObjectContentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LargeObjectContentNotFoundException(Throwable cause) {
        super(cause);
    }

    public LargeObjectContentNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
