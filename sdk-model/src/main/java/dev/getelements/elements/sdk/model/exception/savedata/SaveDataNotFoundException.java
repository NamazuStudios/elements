package dev.getelements.elements.sdk.model.exception.savedata;

import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.savedata.SaveDataDocument;

/**
 * Thrown when an instance of {@link SaveDataDocument} can't be found.
 */
public class SaveDataNotFoundException extends NotFoundException {

    /** Creates a new instance. */
    public SaveDataNotFoundException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public SaveDataNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public SaveDataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public SaveDataNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public SaveDataNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
