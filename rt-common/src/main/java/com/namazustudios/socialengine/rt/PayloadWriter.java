package com.namazustudios.socialengine.rt;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Handles the actual details of writing to the {@link OutputStream}.  Allows for the configuration
 * of multiple Content-Types associated with a {@link Response}.
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

}
