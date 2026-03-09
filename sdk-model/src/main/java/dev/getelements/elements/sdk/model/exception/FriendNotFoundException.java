package dev.getelements.elements.sdk.model.exception;

/** Thrown when a friend relationship cannot be found. */
public class FriendNotFoundException extends NotFoundException {

    /** Creates a new instance. */
    public FriendNotFoundException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public FriendNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public FriendNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public FriendNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public FriendNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
