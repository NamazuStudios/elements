package dev.getelements.elements.exception.blockchain;


import dev.getelements.elements.exception.InvalidDataException;

public class ContractInvocationException extends InvalidDataException {

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
