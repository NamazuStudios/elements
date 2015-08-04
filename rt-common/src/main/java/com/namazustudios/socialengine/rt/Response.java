package com.namazustudios.socialengine.rt;

import javax.validation.Payload;

/**
 * Created by patricktwohig on 7/27/15.
 */
public interface Response {

    /**
     * Gets the response header.
     *
     * @return the response header.
     */
    ResponseHeader getResponseHeader();

    /**
     * Gets the payload of the response.
     */
    Object getPayload();

    /**
     * Sets the payload.
     *
     * @param payload
     */
    void setPayload(Object payload);

}
