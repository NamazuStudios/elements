package com.namazustudios.socialengine.rt;

import javax.validation.Payload;

/**
 * Created by patricktwohig on 7/31/15.
 */
public class SimpleResponse<PayloadT> implements Response<PayloadT> {

    private ResponseHeader responseHeader;

    private PayloadT payload;

    @Override
    public ResponseHeader getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(ResponseHeader responseHeader) {
        this.responseHeader = responseHeader;
    }

    @Override
    public PayloadT getPayload() {
        return payload;
    }

    public void setPayload(PayloadT payload) {
        this.payload = payload;
    }

}
