package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 7/26/15.
 */
public class SimpleRequest<PayloadT> implements Request<PayloadT> {

    private RequestHeader header;

    private PayloadT payload;

    @Override
    public RequestHeader getHeader() {
        return header;
    }

    public void setHeader(RequestHeader header) {
        this.header = header;
    }

    @Override
    public PayloadT getPayload() {
        return payload;
    }

    public void setPayload(PayloadT payload) {
        this.payload = payload;
    }

}
