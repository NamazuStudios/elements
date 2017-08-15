package com.namazustudios.socialengine.rt;

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
     * Gets the payoad, cast to the given type.
     *
     * @param cls the type
     * @param <T>
     */
    <T> T getPayload(Class<T> cls);

}
