package dev.getelements.elements.sdk.model.exception.blockchain;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class NeoWalletNotFoundException extends NotFoundException {
    public NeoWalletNotFoundException() {}

    public NeoWalletNotFoundException(String message) {
        super(message);
    }

    public NeoWalletNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NeoWalletNotFoundException(Throwable cause) {
        super(cause);
    }

    public NeoWalletNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
