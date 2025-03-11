package dev.getelements.elements.sdk.model.exception.blockchain;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class NeoSmartContractNotFoundException extends NotFoundException {
    public NeoSmartContractNotFoundException() {}

    public NeoSmartContractNotFoundException(String message) {
        super(message);
    }

    public NeoSmartContractNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NeoSmartContractNotFoundException(Throwable cause) {
        super(cause);
    }

    public NeoSmartContractNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
