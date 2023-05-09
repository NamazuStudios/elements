package dev.getelements.elements.rt.http;

import dev.getelements.elements.rt.Response;
import dev.getelements.elements.rt.ResponseHeader;

/**
 * Implements {@link HttpResponse} as generated from a {@link HttpRequest} and an underlying {@link Response}.  Most
 * of the information in the {@link HttpResponse} can be derived from the originating {@link HttpRequest} so this
 * simply bridges the two together and selectively delegates to each instance as necessary.
 */
public class CompositeHttpResponse implements HttpResponse {

    private final HttpRequest httpRequest;

    private final Response response;

    public CompositeHttpResponse(HttpRequest httpRequest, Response response) {
        this.httpRequest = httpRequest;
        this.response = response;
    }

    @Override
    public HttpManifestMetadata getManifestMetadata() {
        return httpRequest.getManifestMetadata();
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

}
