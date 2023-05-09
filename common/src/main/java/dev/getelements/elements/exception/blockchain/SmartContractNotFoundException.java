package dev.getelements.elements.exception.blockchain;

import dev.getelements.elements.exception.NotFoundException;

public class SmartContractNotFoundException extends NotFoundException {

    public SmartContractNotFoundException() {}

    public SmartContractNotFoundException(String message) {
        super(message);
    }

    public SmartContractNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmartContractNotFoundException(Throwable cause) {
        super(cause);
    }

    public SmartContractNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
