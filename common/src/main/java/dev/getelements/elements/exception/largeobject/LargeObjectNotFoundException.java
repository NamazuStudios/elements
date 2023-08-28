package dev.getelements.elements.exception.largeobject;

import dev.getelements.elements.exception.NotFoundException;

public class LargeObjectNotFoundException extends NotFoundException {

    public LargeObjectNotFoundException() {}

    public LargeObjectNotFoundException(String message) {
        super(message);
    }

    public LargeObjectNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public LargeObjectNotFoundException(Throwable cause) {
        super(cause);
    }

    public LargeObjectNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
