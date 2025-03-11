package dev.getelements.elements.sdk.exception;

public class InvalidDesignationException extends IllegalArgumentException {

    public InvalidDesignationException() {}

    public InvalidDesignationException(String s) {
        super(s);
    }

    public InvalidDesignationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidDesignationException(Throwable cause) {
        super(cause);
    }

}
