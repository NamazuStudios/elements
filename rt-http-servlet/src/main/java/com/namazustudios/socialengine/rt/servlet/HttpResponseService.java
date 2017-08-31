package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.ResponseHeader;
import com.namazustudios.socialengine.rt.http.HttpManifestMetadata;
import com.namazustudios.socialengine.rt.http.HttpRequest;
import com.namazustudios.socialengine.rt.http.HttpResponse;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface HttpResponseService {

    /**
     * Formulates an instance of {@link  HttpResponse} from the supplied {@link HttpRequest} and
     * underlying {@link Response}.  The default implementation simply returns an instance
     * which delegates to both the {@link HttpRequest} and the {@link Response}.
     *
     * This selects the appropriate {@link HttpContent} using the {@link HttpOperation} supplied
     * byt {@link HttpRequest#getResponseContent()}.
     *
     * @param httpRequest
     * @param response
     *
     * @return the {@link HttpResponse}, assembled from what's known of the {@link HttpRequest}.
     */
    default HttpResponse assemble(final HttpRequest httpRequest, final Response response) {
        return new HttpResponse() {

            @Override
            public HttpManifestMetadata getManifestMetadata() {

                final HttpManifestMetadata manifestMetadata = httpRequest.getManifestMetadata();

                return new HttpManifestMetadata() {

                    @Override
                    public HttpManifest getManifest() {
                        return manifestMetadata.getManifest();
                    }

                    @Override
                    public boolean hasOperation() {
                        return manifestMetadata.hasOperation();
                    }

                    @Override
                    public HttpOperation getOperation() {
                        return manifestMetadata.getOperation();
                    }

                    @Override
                    public List<HttpOperation> getAvailableOperations() {
                        return manifestMetadata.getAvailableOperations();
                    }

                    @Override
                    public HttpContent getContent() {
                        return httpRequest.getResponseContent();
                    }

                    @Override
                    public HttpContent getContentFor(HttpOperation operation) {
                        return manifestMetadata.getContentFor(operation);
                    }

                };
            }

            @Override
            public ResponseHeader getResponseHeader() {
                return response.getResponseHeader();
            }

            @Override
            public Object getPayload() {
                return response.getPayload();
            }

            @Override
            public <T> T getPayload(Class<T> cls) {
                return response.getPayload(cls);
            }

        };
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
     * Writes the supplied {@link HttpResponse} to the supplied {@link HttpServletResponse}.
     *
     * @param httpServletRequest httpServletRequest
     * @param toWrite the {@link HttpResponse} to write
     * @param destination the {@link HttpServletResponse} to receive
     */
    void writeRaw(HttpServletRequest httpServletRequest, Response toWrite, HttpServletResponse destination);

}
