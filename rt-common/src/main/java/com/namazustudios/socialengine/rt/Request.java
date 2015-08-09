package com.namazustudios.socialengine.rt;

/**
 * A Request is a request sent to a particular {@link Resource}
 *
 * This ties the {@link RequestHeader} to a specific payload type for
 * the application.
 *
 * Created by patricktwohig on 7/26/15.
 */
public interface Request {

    /**
     * Gets ths {@link RequestHeader} object for this particular request.
     *
     * @return the header
     */
    RequestHeader getHeader();

    /**
     * Gets the payload object for this Request.
     *
     * @return the paylaod
     */
    Object getPayload();

}
