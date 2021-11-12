package com.namazustudios.socialengine.exception.blockchain;

import com.namazustudios.socialengine.exception.NotFoundException;

public class MongoNeoWalletNotFoundException extends NotFoundException {
    public MongoNeoWalletNotFoundException() {}

    public MongoNeoWalletNotFoundException(String message) {
        super(message);
    }

    public MongoNeoWalletNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MongoNeoWalletNotFoundException(Throwable cause) {
        super(cause);
    }

    public MongoNeoWalletNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
