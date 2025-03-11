package dev.getelements.elements.sdk.model.exception.inventory;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class DistinctInventoryItemNotFoundException extends NotFoundException {

    public DistinctInventoryItemNotFoundException() {}

    public DistinctInventoryItemNotFoundException(String message) {
        super(message);
    }

    public DistinctInventoryItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public DistinctInventoryItemNotFoundException(Throwable cause) {
        super(cause);
    }

    public DistinctInventoryItemNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
