package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.rt.ResponseCode;
import com.namazustudios.socialengine.rt.id.ResourceId;

public class DeadResourceException extends BaseException {

    public DeadResourceException() {
    }

    public DeadResourceException(String message) {
        super(message);
    }

    public DeadResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeadResourceException(Throwable cause) {
        super(cause);
    }

    public DeadResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.RESOURCE_DEAD;
    }

}
