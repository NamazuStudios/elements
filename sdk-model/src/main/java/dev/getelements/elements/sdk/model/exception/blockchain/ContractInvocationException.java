package dev.getelements.elements.sdk.model.exception.blockchain;


import dev.getelements.elements.sdk.model.exception.InvalidDataException;

/** Thrown when a smart contract invocation fails. */
public class ContractInvocationException extends InvalidDataException {

    /** Creates a new instance. */
    public ContractInvocationException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public ContractInvocationException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public ContractInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public ContractInvocationException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public ContractInvocationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
