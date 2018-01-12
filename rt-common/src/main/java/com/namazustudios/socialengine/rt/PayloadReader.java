package com.namazustudios.socialengine.rt;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Handles the actual details of writing to the {@link OutputStream}.  Allows for multiple content types
 * to be read throughthe {@link Request} and {@link Request}
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
    <T> T read(Class<T> payloadType, InputStream stream) throws IOException;

    /**
     * Reads a payload from the supplied byte array.  The default implementation of this method uses a temporary
     * {@link ByteArrayInputStream} to accomplish the deserialization.
     *
     * @param payloadType the type of the payload
     * @param toRead the byte array to read
     * @param <T> the type of the payload
     * @return the deserialized payload
     * @throws IOException
     */
    default <T> T read(Class<T> payloadType, byte[] toRead) throws IOException {
        try (final ByteArrayInputStream bis = new ByteArrayInputStream(toRead)) {
            return read(payloadType, bis);
        }
    }

}
