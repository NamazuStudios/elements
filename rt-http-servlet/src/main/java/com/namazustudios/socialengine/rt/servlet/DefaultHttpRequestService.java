package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.UnacceptableContentException;
import com.namazustudios.socialengine.rt.http.HttpRequest;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

public class DefaultHttpRequestService implements HttpRequestService {

    private Supplier<HttpManifest> httpManifestSupplier;

    private Map<String, PayloadReader> paylaodReadersByContentType;

    @Override
    public HttpRequest getRequest(final HttpServletRequest req) {
        final HttpManifest httpManifest = getHttpManifestSupplier().get();
        return new ServletHttpRequest(req, httpManifest, c -> deserialize(c, req));
    }

    private Object deserialize(final HttpContent httpContent, final HttpServletRequest req) {

        final String contentType = httpContent.getType();
        final Class<?> payloadClass = httpContent.getPayloadType();

        final PayloadReader payloadReader = getPaylaodReadersByContentType().get(contentType);

        if (payloadReader == null) {
            throw new UnacceptableContentException("no reader configured for " + contentType);
        }

        try {
            return payloadReader.read(payloadClass, req.getInputStream());
        } catch (IOException ex) {
            throw new InternalException(ex);
        }

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
