package com.namazustudios.socialengine.exception.blockchain;

import com.namazustudios.socialengine.exception.NotFoundException;

public class Neow3jClientException  extends NotFoundException {
    public Neow3jClientException() {}

    public Neow3jClientException(String message) {
        super(message);
    }

    public Neow3jClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public Neow3jClientException(Throwable cause) {
        super(cause);
    }

    public Neow3jClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
