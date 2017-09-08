package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.http.HttpResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Handles the actual details of writing to the {@link OutputStream}.  Allows for the configuration
 * of multiple Content-Types associated with a {@link HttpResponse}.
 */
@FunctionalInterface
public interface PayloadWriter {

    /**
     * Writes the actual response object to the {@link OutputStream}.
     *
     * @param payload the the response object
     * @param stream the output stream
     * @throws ServletException
     * @throws IOException
     */
    void write(Object payload, OutputStream stream) throws IOException;

}
