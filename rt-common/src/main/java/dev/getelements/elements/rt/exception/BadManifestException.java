package dev.getelements.elements.rt.exception;

import dev.getelements.elements.rt.ResponseCode;

import static java.lang.String.format;

/**
 * Created by patricktwohig on 8/16/17.
 */
public class BadManifestException extends BaseException {

    private final Object details;

    public BadManifestException() {
        this.details = null;
    }

    public BadManifestException(String message) {
        super(message);
        this.details = null;
    }

    public BadManifestException(String message, Throwable cause) {
        super(message, cause);
        this.details = null;
    }

    public BadManifestException(Throwable cause) {
        super(cause);
        this.details = null;
    }

    public BadManifestException(Object details) {
        super(format("Details: [%s]", details.toString()));
        this.details = details;
    }

    public BadManifestException(Object details, Throwable cause) {
        super(format("Details: [%s]", details.toString()));
        this.details = details;
    }

    public BadManifestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.details = null;
    }

    public Object getDetails() {
        return details;
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.INTERNAL_ERROR_BAD_MANIFEST_FATAL;
    }

}
