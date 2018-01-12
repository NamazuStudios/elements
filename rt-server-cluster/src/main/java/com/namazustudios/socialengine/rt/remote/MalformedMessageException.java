package com.namazustudios.socialengine.rt.remote;


import com.namazustudios.socialengine.rt.exception.InternalException;

public class MalformedMessageException extends InternalException {

    public MalformedMessageException() {}

    public MalformedMessageException(String message) {
        super(message);
    }

    public MalformedMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public MalformedMessageException(Throwable cause) {
        super(cause);
    }

    public MalformedMessageException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
