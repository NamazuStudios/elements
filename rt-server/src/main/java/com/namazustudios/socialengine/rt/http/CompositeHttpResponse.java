package com.namazustudios.socialengine.rt.http;

import com.namazustudios.socialengine.rt.Response;
import com.namazustudios.socialengine.rt.ResponseHeader;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;

import java.util.List;

/**
 * Implements {@link HttpResponse} as generated from a {@link HttpRequest} and an underlying {@link Response}.
 *
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

}
