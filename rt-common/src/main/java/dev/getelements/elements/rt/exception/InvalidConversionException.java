package dev.getelements.elements.rt.exception;

/**
 * Thrown by methods which may convert values. This indicates that a conversion is not
 * available or not possible.
 */
public class InvalidConversionException extends RuntimeException {

    public InvalidConversionException() {}

    public InvalidConversionException(String message) {
        super(message);
    }

    public InvalidConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidConversionException(Throwable cause) {
        super(cause);
    }

    public InvalidConversionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
