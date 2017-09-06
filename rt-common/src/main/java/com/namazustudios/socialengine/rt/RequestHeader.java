package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.BadRequestException;

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

    /**
     * Returns the value of {@link #getPath()} as a fully parsed {@link Path} object.
     *
     * @return the {@link Path} object
     *
     * @throws {@link BadRequestException} if the value of {@link #getPath()} does not parse
     */
    default Path getParsedPath() throws BadRequestException {
        try {
            return new Path(getPath());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex);
        }
    }

}
