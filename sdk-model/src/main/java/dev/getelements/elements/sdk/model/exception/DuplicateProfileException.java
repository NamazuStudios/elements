package dev.getelements.elements.sdk.model.exception;

public class DuplicateProfileException extends DuplicateException {

    public DuplicateProfileException() {
        super();
    }

    public DuplicateProfileException(String message) {
        super(message);
    }

    public DuplicateProfileException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateProfileException(Throwable cause) {
        super(cause);
    }

    public DuplicateProfileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
