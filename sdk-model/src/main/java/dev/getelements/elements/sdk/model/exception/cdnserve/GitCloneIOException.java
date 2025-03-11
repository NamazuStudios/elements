package dev.getelements.elements.sdk.model.exception.cdnserve;

import java.io.IOException;

public class GitCloneIOException extends IOException {

    public GitCloneIOException() {}

    public GitCloneIOException(String message) {
        super(message);
    }

    public GitCloneIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public GitCloneIOException(Throwable cause) {
        super(cause);
    }
}
