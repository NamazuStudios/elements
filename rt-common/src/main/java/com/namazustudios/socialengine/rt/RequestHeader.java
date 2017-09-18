package com.namazustudios.socialengine.rt;

import java.util.List;
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
public interface RequestHeader {

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

    /**
     * Gets a listing of headers mapped by header name.  A header may be repeated and therefore
     * the header may not have the
     *
     * @return the mapping of headers.
     */
    Map<String, List<String>> getHeaders();

    /**
     * Gets a single header with the supplied name.
     *
     * @param header the header
     * @return the header value, or null if no header is found
     */
    default String getHeader(final String header) {
        final List<String> headers = getHeaders().get(header);
        return headers == null || headers.isEmpty() ? null : headers.get(0);
    }

}
