package dev.getelements.elements.exception.blockchain;

import dev.getelements.elements.exception.NotFoundException;

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
