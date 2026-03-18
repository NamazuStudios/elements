package dev.getelements.elements.sdk.model.exception.cdnserve;

import java.io.IOException;

/** Thrown when a symbolic link IO operation fails. */
public class SymbolicLinkIOException extends IOException {

    /** Creates a new instance. */
    public SymbolicLinkIOException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public SymbolicLinkIOException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public SymbolicLinkIOException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public SymbolicLinkIOException(Throwable cause) {
        super(cause);
    }
}
