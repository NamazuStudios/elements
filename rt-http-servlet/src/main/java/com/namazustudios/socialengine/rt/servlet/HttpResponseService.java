package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.http.CompositeHttpResponse;
import com.namazustudios.socialengine.rt.http.HttpManifestMetadata;
import com.namazustudios.socialengine.rt.http.HttpRequest;
import com.namazustudios.socialengine.rt.http.HttpResponse;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface HttpResponseService {

    /**
     * Formulates an instance of {@link  HttpResponse} from the supplied {@link HttpRequest} and
     * underlying {@link Response}.  The default implementation simply returns an instance
     * which delegates to both the {@link HttpRequest} and the {@link Response}.
     *
     * This selects the appropriate {@link HttpContent} using the {@link HttpOperation} supplied
     * byt {@link HttpManifestMetadata#getPreferredResponseContent()}.
     *
     * @param httpRequest
     * @param response
     *
     * @return the {@link HttpResponse}, assembled from what's known of the {@link HttpRequest}.
     */
    default HttpResponse assemble(final HttpRequest httpRequest, final Response response) {
        return new CompositeHttpResponse(httpRequest, response);
    }

    /**
     * Assembles the supplied a {@link HttpResponse} from the supplied {@link HttpRequest} and {@link Response}
     * and then writes it to the supplied {@link HttpServletResponse}.
     *
     * The default implementation accmplishes this by using {@link #assemble(HttpRequest, Response)} and then
     * calling {@link #write(HttpResponse, HttpServletResponse)}.
     *
     * @param httpRequest the {@link HttpRequest} which triggered the {@link Response}
     * @param toWrite the {@link Response} to write
     * @param destination the destination of the {@link Response}
     */
    default void assembleAndWrite(final HttpRequest httpRequest,
                                  final Response toWrite,
                                  final HttpServletResponse destination) {
        final HttpResponse httpResponse = assemble(httpRequest, toWrite);
        write(httpResponse, destination);
    }

    /**
     * Writes the supplied {@link HttpResponse} to the supplied {@link HttpServletResponse}.
     *
     * @param toWrite the {@link HttpResponse} to write
     * @param destination the {@link HttpServletResponse} to receive
     */
    void write(HttpResponse toWrite, HttpServletResponse destination);

    /**
     * Handles the actual details of writing to the {@link HttpServletResponse}.  Allows for the configuration
     * of multiple Content-Types associated with a {@link HttpResponse}.
     */
    @FunctionalInterface
    interface EntityBodyWriter {

        /**
         * Writes the actual response object to the {@link HttpServletResponse}.
         *
         * @param payload the the response object
         * @param destination t
         * @throws ServletException
         * @throws IOException
         */
        void writeEntityBody(Object payload, HttpServletResponse destination) throws ServletException, IOException;

    }

}
