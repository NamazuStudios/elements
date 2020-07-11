package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;

public class UnixFSNoSuchRevisionException extends InternalException {

    public UnixFSNoSuchRevisionException() {}

    public UnixFSNoSuchRevisionException(String message) {
        super(message);
    }

    public UnixFSNoSuchRevisionException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnixFSNoSuchRevisionException(Throwable cause) {
        super(cause);
    }

    public UnixFSNoSuchRevisionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
