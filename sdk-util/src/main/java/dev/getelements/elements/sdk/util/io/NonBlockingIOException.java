package dev.getelements.elements.rt.util;

import java.io.IOException;

/**
 * A special type of {@link IOException} used by the {@link InputStreamAdapter} and the {@link OutputStreamAdapter} to
 * indicate that the backed channel is currently in a non-blocking mode.
 */
public class NonBlockingIOException extends IOException {

    public NonBlockingIOException() {}

    public NonBlockingIOException(String message) {
        super(message);
    }

    public NonBlockingIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonBlockingIOException(Throwable cause) {
        super(cause);
    }

}
