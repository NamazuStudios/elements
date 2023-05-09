package dev.getelements.elements.exception;

/**
 * Thrown when a bad search query is encountered.
 *
 * Created by patricktwohig on 5/17/15.
 */
public class BadQueryException extends InvalidDataException {

    public BadQueryException() {}

    public BadQueryException(String message) {
        super(message);
    }

    public BadQueryException(String message, Object model) {
        super(message, model);
    }

    public BadQueryException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadQueryException(String message, Throwable cause, Object model) {
        super(message, cause, model);
    }

    public BadQueryException(Throwable cause) {
        super(cause);
    }

    public BadQueryException(Throwable cause, Object model) {
        super(cause, model);
    }

    public BadQueryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
