package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;

public class ChecksumFailureExeception extends InternalException {

    public ChecksumFailureExeception() {}

    public ChecksumFailureExeception(String message) {
        super(message);
    }

    public ChecksumFailureExeception(String message, Throwable cause) {
        super(message, cause);
    }

    public ChecksumFailureExeception(Throwable cause) {
        super(cause);
    }

    public ChecksumFailureExeception(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
