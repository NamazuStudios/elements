package dev.getelements.elements.rt.exception;

public class InvalidPemException extends Exception {

    public InvalidPemException() {}

    public InvalidPemException(String message) {
        super(message);
    }

    public InvalidPemException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPemException(Throwable cause) {
        super(cause);
    }

    public InvalidPemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
