package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.http.HttpRequest;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.function.Supplier;

public class DefaultHttpRequestService implements HttpRequestService {

    private Supplier<HttpManifest> httpManifestSupplier;

    private Map<String, PayloadReader> paylaodReadersByContentType;

    @Override
    public HttpRequest getRequest(final HttpServletRequest req) {
        final HttpManifest httpManifest = getHttpManifestSupplier().get();
        return new ServletHttpRequest(req, httpManifest, this::deserialize);
    }

    private Object deserialize(final HttpContent httpContent) {

        final String contentType = httpContent.getType();
        final Class<?> payloadClass = httpContent.getPayloadType();

        final PayloadReader payloadReader = getPaylaodReadersByContentType().get(contentType);

        if (payloadReader == null) {

        }

        return null;


    }

    public Supplier<HttpManifest> getHttpManifestSupplier() {
        return httpManifestSupplier;
    }

    @Inject
    public void setHttpManifestSupplier(Supplier<HttpManifest> httpManifestSupplier) {
        this.httpManifestSupplier = httpManifestSupplier;
    }

    public Map<String, PayloadReader> getPaylaodReadersByContentType() {
        return paylaodReadersByContentType;
    }

    @Inject
    public void setPaylaodReadersByContentType(Map<String, PayloadReader> paylaodReadersByContentType) {
        this.paylaodReadersByContentType = paylaodReadersByContentType;
    }

}
