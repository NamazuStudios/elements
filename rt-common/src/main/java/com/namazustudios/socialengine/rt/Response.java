package com.namazustudios.socialengine.rt;

import javax.validation.Payload;

/**
 * Created by patricktwohig on 7/27/15.
 */
public interface Response<PayloadT> {

    /**
     * Gets the response header.
     *
     * @return the response header.
     */
    ResponseHeader getResponseHeader();

    /**
     * Gets the payload of the response.
     */
    PayloadT getPayload();

}
