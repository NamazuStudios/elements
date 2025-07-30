package dev.getelements.elements.sdk.model.exception.metadata;

import dev.getelements.elements.sdk.model.exception.NotFoundException;

public class MetadataNotFoundException extends NotFoundException {

    public MetadataNotFoundException() {}

    public MetadataNotFoundException(String message) {
        super(message);
    }

    public MetadataNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MetadataNotFoundException(Throwable cause) {
        super(cause);
    }

    public MetadataNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
