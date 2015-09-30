package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.exception.BaseException;
import com.namazustudios.socialengine.exception.ErrorCode;

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
    public ErrorCode getCode() {
        return ErrorCode.INVALID_DATA;
    }

}
