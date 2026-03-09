package dev.getelements.elements.sdk.model.exception.cdnserve;

import java.io.IOException;

/** Thrown when a git clone IO operation fails. */
public class GitCloneIOException extends IOException {

    /** Creates a new instance. */
    public GitCloneIOException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public GitCloneIOException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public GitCloneIOException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public GitCloneIOException(Throwable cause) {
        super(cause);
    }
}
