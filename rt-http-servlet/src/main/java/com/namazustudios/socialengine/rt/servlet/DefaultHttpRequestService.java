package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.UnacceptableContentException;
import com.namazustudios.socialengine.rt.http.HttpRequest;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;

import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class DefaultHttpRequestService implements HttpRequestService {

    private ManifestLoader manifestLoader;

    private Map<String, PayloadReader> paylaodReadersByContentType;

    @Override
    public HttpRequest getRequest(HttpServletRequest req) {

        final HttpManifest httpManifest = getManifestLoader().getHttpManifest();

        final Supplier<HttpServletRequest> httpServletRequestSupplier;
        httpServletRequestSupplier = () -> req;

        final Function<HttpContent, Object> payloadDeserializerFunction;
        payloadDeserializerFunction = content -> deserialize(content, req);

        return new ServletHttpRequest(httpManifest, httpServletRequestSupplier, payloadDeserializerFunction);

    }

    @Override
    public HttpRequest getAsyncRequest(final AsyncContext asyncContext) {

        final HttpManifest httpManifest = getManifestLoader().getHttpManifest();

        final Supplier<HttpServletRequest> httpServletRequestSupplier;
        httpServletRequestSupplier = () -> (HttpServletRequest)asyncContext.getRequest();

        final Function<HttpContent, Object> payloadDeserializerFunction;
        payloadDeserializerFunction = content -> deserialize(content, httpServletRequestSupplier.get());

        return new ServletHttpRequest(httpManifest, httpServletRequestSupplier, payloadDeserializerFunction);

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

    public ManifestLoader getManifestLoader() {
        return manifestLoader;
    }

    @Inject
    public void setManifestLoader(ManifestLoader manifestLoader) {
        this.manifestLoader = manifestLoader;
    }

    public Map<String, PayloadReader> getPaylaodReadersByContentType() {
        return paylaodReadersByContentType;
    }

    @Inject
    public void setPaylaodReadersByContentType(Map<String, PayloadReader> paylaodReadersByContentType) {
        this.paylaodReadersByContentType = paylaodReadersByContentType;
    }

}
