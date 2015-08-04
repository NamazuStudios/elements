package com.namazustudios.socialengine.rt;

/**
 *
 * The basic response type for the RT Server.  This is compact set of
 * metadata for a single request.  This has some terminology similar to
 * HTTP.
 *
 * Note that this really only includes the information preceeding a request
 * and the actual payload of the request follows this object in the stream
 * or packet.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface ResponseHeader {

    /**
     * Gets the response code.
     *
     * @return
     */
    int getCode();

    /**
     * Gets the sequence of the response.
     *
     * @return the sequence
     */
    int getSequence();

}
