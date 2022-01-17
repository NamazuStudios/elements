package com.namazustudios.socialengine.exception.blockchain;

import com.namazustudios.socialengine.exception.NotFoundException;

public class ContractInvocationException extends NotFoundException {
    public ContractInvocationException() {}

    public ContractInvocationException(String message) {
        super(message);
    }

    public ContractInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContractInvocationException(Throwable cause) {
        super(cause);
    }

    public ContractInvocationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
