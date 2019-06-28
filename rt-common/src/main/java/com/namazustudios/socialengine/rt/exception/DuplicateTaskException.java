package com.namazustudios.socialengine.rt.exception;

import com.namazustudios.socialengine.rt.ResponseCode;

public class DuplicateTaskException extends BaseException {

    @Override
    public ResponseCode getResponseCode() {
        return ResponseCode.DUPLICATE_TASK;
    }
}
