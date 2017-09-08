package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.http.HttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Handles the actual details of writing to the {@link OutputStream}.  Allows for multiple content types
 * to be read throughthe {@link Request} and {@link HttpRequest}
 */
@FunctionalInterface
public interface PayloadReader {

    /**
     * Reads the actual response object to the {@link InputStream}.
     *
     * @param payloadType the the response object
     * @param stream the output stream
     * @throws IOException
     */
    Object read(Class<?> payloadType, InputStream stream) throws IOException;

}
