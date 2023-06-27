package dev.getelements.elements.rt.exception;

import dev.getelements.elements.rt.ResponseCode;

/**
 * Created by patricktwohig on 9/30/15.
 */
public class CorruptEnvelopeException extends BaseException {

    public CorruptEnvelopeException() {}

    public CorruptEnvelopeException(String message) {
        super(message);
    }

    public CorruptEnvelopeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CorruptEnvelopeException(Throwable cause) {
        super(cause);
    }

    public CorruptEnvelopeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.BAD_REQUEST_RETRY;
    }

}
