package dev.getelements.elements.sdk.cluster.path.exception;

/**
 * Thrown when a {@link dev.getelements.elements.sdk.cluster.path.Path} cannot be parsed or constructed, or when
 * an operation is attempted on a {@link dev.getelements.elements.sdk.cluster.path.Path} that violates its
 * invariants.
 */
public class InvalidPathException extends IllegalArgumentException {

    public InvalidPathException() {}

    public InvalidPathException(String s) {
        super(s);
    }

    public InvalidPathException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPathException(Throwable cause) {
        super(cause);
    }
}
