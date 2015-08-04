package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 7/31/15.
 */
public class SimpleResponse implements Response {

    private ResponseHeader responseHeader;

    private Object payload;

    @Override
    public ResponseHeader getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(ResponseHeader responseHeader) {
        this.responseHeader = responseHeader;
    }

    @Override
    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

}
