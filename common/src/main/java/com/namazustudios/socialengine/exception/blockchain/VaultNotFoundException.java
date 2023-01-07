package com.namazustudios.socialengine.exception.blockchain;

import com.namazustudios.socialengine.exception.NotFoundException;

public class VaultNotFoundException extends NotFoundException {

    public VaultNotFoundException() {}

    public VaultNotFoundException(String message) {
        super(message);
    }

    public VaultNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public VaultNotFoundException(Throwable cause) {
        super(cause);
    }

    public VaultNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
