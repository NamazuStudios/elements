package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.rt.ResponseCode;

public class AsyncOperationCanceledException extends BaseException {

    public AsyncOperationCanceledException() {}

    public AsyncOperationCanceledException(String message) {
        super(message);
    }

    public AsyncOperationCanceledException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsyncOperationCanceledException(Throwable cause) {
        super(cause);
    }

    public AsyncOperationCanceledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.ASYNC_OPERATION_CANCELED;
    }

}
