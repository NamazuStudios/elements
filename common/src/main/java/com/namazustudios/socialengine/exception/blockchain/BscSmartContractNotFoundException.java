package com.namazustudios.socialengine.exception.blockchain;

import com.namazustudios.socialengine.exception.NotFoundException;

public class BscSmartContractNotFoundException extends NotFoundException {
    public BscSmartContractNotFoundException() {}

    public BscSmartContractNotFoundException(String message) {
        super(message);
    }

    public BscSmartContractNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BscSmartContractNotFoundException(Throwable cause) {
        super(cause);
    }

    public BscSmartContractNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
