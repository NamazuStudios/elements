package dev.getelements.elements.exception.schema;

import dev.getelements.elements.exception.NotFoundException;

public class TokenTemplateNotFoundException extends NotFoundException {
    public TokenTemplateNotFoundException() {}

    public TokenTemplateNotFoundException(String message) {
        super(message);
    }

    public TokenTemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenTemplateNotFoundException(Throwable cause) {
        super(cause);
    }

    public TokenTemplateNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
