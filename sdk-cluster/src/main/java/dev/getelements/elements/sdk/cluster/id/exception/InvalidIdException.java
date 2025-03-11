package dev.getelements.elements.sdk.cluster.id.exception;

public class InvalidIdException extends IllegalArgumentException {

    public InvalidIdException() {
    }

    public InvalidIdException(String s) {
        super(s);
    }

    public InvalidIdException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidIdException(Throwable cause) {
        super(cause);
    }

}
