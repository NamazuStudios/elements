package com.namazustudios.socialengine.rt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * Handles the actual details of writing an {@link Object} to a byte stream.
 */
@FunctionalInterface
public interface PayloadWriter {

    /**
     * Writes the actual response object to the {@link OutputStream}.
     *
     * @param payload the the response object
     * @param stream the output stream
     *
     * @throws IOException
     */
    void write(Object payload, OutputStream stream) throws IOException;

    /**
     * Writes the supplied {@link Object} payload to a byte array.  The default implementation uses a temporary
     * {@link ByteArrayOutputStream} to accomplish this task.
     *
     * @param payload the {@link Object} payload
     * @return the byte stream representing the {@link Object}
     */
    default byte[] write(final Object payload) throws IOException {
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            write(payload, bos);
            return bos.toByteArray();
        }
    }

}
