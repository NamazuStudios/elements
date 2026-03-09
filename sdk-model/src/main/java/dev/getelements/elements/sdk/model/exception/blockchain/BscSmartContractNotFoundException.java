package dev.getelements.elements.sdk.model.exception.blockchain;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

/** Thrown when a BSC smart contract cannot be found. */
public class BscSmartContractNotFoundException extends NotFoundException {

    /** Creates a new instance. */
    public BscSmartContractNotFoundException() {}

    /**
     * Creates a new instance with the given message.
     * @param message the detail message
     */
    public BscSmartContractNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public BscSmartContractNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance with the given cause.
     * @param cause the cause
     */
    public BscSmartContractNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new instance.
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled
     * @param writableStackTrace whether the stack trace is writable
     */
    public BscSmartContractNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
