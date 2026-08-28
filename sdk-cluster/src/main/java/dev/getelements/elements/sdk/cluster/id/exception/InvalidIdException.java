package dev.getelements.elements.sdk.cluster.id.exception;

/**
 * Thrown when an identifier in the {@code dev.getelements.elements.sdk.cluster.id} package cannot be parsed or
 * constructed. Serves as the common base type for the more specific {@code InvalidXxxIdException} classes.
 */
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
