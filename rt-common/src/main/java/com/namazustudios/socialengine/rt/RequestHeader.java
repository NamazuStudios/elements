package com.namazustudios.socialengine.rt;

/**
 * The basic request type for the RT Server.  This is compact set of
 * metadata for a single request.  This has some terminology similar to
 * HTTP.
 *
 * Note that this really only includes the information preceeding a request
 * and the actual payload of the request follows this object in the stream
 * or packet.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface RequestHeader extends NamedHeaders {

    /**
     * Gets the sequence of the request.  The client, when making the request,
     * will produce a response with this sequence.
     *
     * @return the sequence
     */
    int getSequence();

    /**
     * The method of the resource to invoke.
     *
     * @return the method, never null
     */
    String getMethod();

    /**
     * The path of the resource to dispatch the request.
     *
     * @return the path, never null
     */
    String getPath();

}
