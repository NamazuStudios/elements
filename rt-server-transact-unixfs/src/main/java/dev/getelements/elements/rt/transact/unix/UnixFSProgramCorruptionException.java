package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.rt.transact.FatalException;

public class UnixFSProgramCorruptionException extends FatalException {

    public UnixFSProgramCorruptionException() {}

    public UnixFSProgramCorruptionException(String message) {
        super(message);
    }

    public UnixFSProgramCorruptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnixFSProgramCorruptionException(Throwable cause) {
        super(cause);
    }

    public UnixFSProgramCorruptionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
