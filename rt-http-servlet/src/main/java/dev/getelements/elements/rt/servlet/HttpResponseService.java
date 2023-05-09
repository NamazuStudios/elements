package dev.getelements.elements.rt.servlet;

import dev.getelements.elements.rt.Response;
import dev.getelements.elements.rt.http.CompositeHttpResponse;
import dev.getelements.elements.rt.http.HttpManifestMetadata;
import dev.getelements.elements.rt.http.HttpRequest;
import dev.getelements.elements.rt.http.HttpResponse;
import dev.getelements.elements.rt.manifest.http.HttpContent;
import dev.getelements.elements.rt.manifest.http.HttpOperation;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

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
     * Assembles the supplied a {@link HttpResponse} from the supplied {@link HttpRequest} and {@link Response}
     * and then writes it to the supplied {@link HttpServletResponse}.
     *
     * The default implementation accmplishes this by using {@link #assemble(HttpRequest, Response)} and then
     * calling {@link #write(HttpResponse, HttpServletResponse)}.
     *
     * @param toWrite the {@link Response} to write
     * @param destination the destination of the {@link Response}
     */
    void write(HttpServletRequest httpServletRequest, Response toWrite, HttpServletResponse destination);

}
