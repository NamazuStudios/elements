package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.rt.ResponseCode;

/**
 * Created by patricktwohig on 8/15/17.
 */
public class ManifestNotFoundException extends BaseException {

    public ManifestNotFoundException() {
    }

    public ManifestNotFoundException(String message) {
        super(message);
    }

    public ManifestNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManifestNotFoundException(Throwable cause) {
        super(cause);
    }

    public ManifestNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.MANIFEST_NOT_FOUND;
    }

}
