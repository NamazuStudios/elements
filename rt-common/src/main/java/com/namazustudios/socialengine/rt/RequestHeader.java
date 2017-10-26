package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.BadRequestException;

import java.util.Collections;
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
public interface RequestHeader extends NamedHeaders {

    /**
     * Denotes an unknown sequence.
     */
    int UNKNOWN_SEQUENCE = -1;

    /**
     * Gets the sequence of the request.  The client, when making the request,
     * will produce a response with this sequence.  The sequence may be -1 to
     * indicate that no sequencing is used fo this paticular {@link Request}.
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

    /**
     * If the associated {@link Request} was built with a {@link ParameterizedPath}, then this will return the
     * parameters used to formulate the {@link Request}.  The default implementation assumes this {@link RequestHeader}
     * was formulated without a {@link ParameterizedPath} and returns the value of {@link Collections#emptyMap()}.
     * Implementations should provide more detail if such detail is available.
     *
     * @return the path parameters where the key is the parameter name and the value is the matching path component
     */
    default Map<String, String> getPathParameters() {
        return Collections.emptyMap();
    }

}
