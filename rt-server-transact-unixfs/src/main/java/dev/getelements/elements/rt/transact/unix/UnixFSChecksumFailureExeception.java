package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.exception.InternalException;

public class UnixFSChecksumFailureExeception extends InternalException {

    public UnixFSChecksumFailureExeception() {}

    public UnixFSChecksumFailureExeception(String message) {
        super(message);
    }

    public UnixFSChecksumFailureExeception(String message, Throwable cause) {
        super(message, cause);
    }

    public UnixFSChecksumFailureExeception(Throwable cause) {
        super(cause);
    }

    public UnixFSChecksumFailureExeception(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
