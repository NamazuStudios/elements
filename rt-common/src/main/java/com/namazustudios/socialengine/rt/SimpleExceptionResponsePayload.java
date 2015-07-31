package com.namazustudios.socialengine.rt;

/**
 * A simple error model to indicate an exception was caught.
 *
 * Created by patricktwohig on 7/31/15.
 */
public class SimpleExceptionResponsePayload {

    public String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
