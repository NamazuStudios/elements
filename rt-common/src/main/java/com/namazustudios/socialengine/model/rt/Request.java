package com.namazustudios.socialengine.model.rt;

import java.util.Map;

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
public interface Request {

    /**
     * Gets the sequence of the request.  The client, when making the request,
     * will produce a response with this sequence.
     *
     * @return the sequence
     */
    int getSequence();

    /**
     * The method of the resource.  Note that unlke HTTP this is completely
     * user-defined.
     *
     * @return the method, never null
     */
    String getMethod();

    /**
     * The path of the resource to receive the request.
     *
     * @return the path, never null
     */
    String getPath();

    /**
     * Gets a listing of headers mapped by header name.  Note that only one
     * header name may be used.  Header names may also be reordered.  There
     * is no guarantee that a header be in a particular order.
     *
     * Headers are specific for the particular request and are user-defined.
     * They may not be transmitted as strings, but must always be represented
     * as such.
     *
     * @return the mapping of headers.
     */
    Map<String, String> getHeaders();

}
