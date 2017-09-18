package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.rt.ResponseCode;

/**
 * Created by patricktwohig on 8/16/17.
 */
public class BadManifestException extends BaseException {

    public BadManifestException() {}

    public BadManifestException(String message) {
        super(message);
    }

    public BadManifestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadManifestException(Throwable cause) {
        super(cause);
    }

    public BadManifestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.INTERNAL_ERROR_BAD_MANIFEST_FATAL;
    }

}
