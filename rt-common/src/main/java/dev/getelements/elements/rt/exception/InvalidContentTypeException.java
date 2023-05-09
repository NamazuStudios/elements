package dev.getelements.elements.rt.exception;

import dev.getelements.elements.rt.Request;
import dev.getelements.elements.rt.Response;
import dev.getelements.elements.rt.ResponseCode;
import dev.getelements.elements.rt.manifest.http.HttpContent;

/**
 * Specific to {@link HttpContent} processing.  When thrown, this indicates that the {@link HttpContent}
 * cannot be matched between an {@link Request} and an {@link Response}.
 */
public class InvalidContentTypeException extends BaseException {

    public InvalidContentTypeException() {}

    public InvalidContentTypeException(String message) {
        super(message);
    }

    public InvalidContentTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidContentTypeException(Throwable cause) {
        super(cause);
    }

    public InvalidContentTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.BAD_REQUEST_INVALID_CONTENT;
    }

}
