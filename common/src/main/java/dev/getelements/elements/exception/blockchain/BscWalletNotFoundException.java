package dev.getelements.elements.exception.blockchain;

import dev.getelements.elements.exception.NotFoundException;

public class BscWalletNotFoundException extends NotFoundException {
    public BscWalletNotFoundException() {}

    public BscWalletNotFoundException(String message) {
        super(message);
    }

    public BscWalletNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BscWalletNotFoundException(Throwable cause) {
        super(cause);
    }

    public BscWalletNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
