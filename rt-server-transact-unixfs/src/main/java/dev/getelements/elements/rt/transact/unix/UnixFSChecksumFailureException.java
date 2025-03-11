package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.exception.InternalException;

public class UnixFSChecksumFailureException extends InternalException {

    public UnixFSChecksumFailureException() {}

    public UnixFSChecksumFailureException(String message) {
        super(message);
    }

    public UnixFSChecksumFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnixFSChecksumFailureException(Throwable cause) {
        super(cause);
    }

    public UnixFSChecksumFailureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
