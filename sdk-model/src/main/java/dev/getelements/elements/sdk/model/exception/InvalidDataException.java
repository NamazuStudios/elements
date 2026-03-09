package dev.getelements.elements.sdk.model.exception;

/**
 * Thrown when invalid data or input is provided.
 */
public class InvalidDataException extends BaseException {

    private transient final Object model;

    /** Creates a new instance. */
    public InvalidDataException() {
        this.model = null;
    }

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public InvalidDataException(String message) {
        super(message);
        model = null;
    }

    /**
     * Creates a new instance with the given message and model.
     * @param message the detail message
     * @param model the invalid model object
     */
    public InvalidDataException(String message, Object model) {
        super(message);
        this.model = model;
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
        this.model = null;
    }

    /**
     * Creates a new instance with the given message, cause and model.
     * @param message the detail message
     * @param cause the cause
     * @param model the invalid model object
     */
    public InvalidDataException(String message, Throwable cause, Object model) {
        super(message, cause);
        this.model = null;

    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public InvalidDataException(Throwable cause) {
        super(cause);
        this.model = null;
    }

    /**
     * Creates a new instance with the given cause and model.
     * @param cause the cause
     * @param model the invalid model object
     */
    public InvalidDataException(Throwable cause, Object model) {
        super(cause);
        this.model = model;
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public InvalidDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.model = null;
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.INVALID_DATA;
    }

}
