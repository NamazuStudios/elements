package dev.getelements.elements.rt.servlet;

import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.PayloadReader;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.rt.exception.UnacceptableContentException;
import dev.getelements.elements.rt.http.HttpRequest;
import dev.getelements.elements.rt.manifest.http.HttpContent;
import dev.getelements.elements.rt.manifest.http.HttpManifest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static dev.getelements.elements.rt.Context.REMOTE;

public class DefaultHttpRequestService implements HttpRequestService {

    private Context context;

    private Map<String, PayloadReader> payloadReadersByContentType;

    @Override
    public HttpRequest getRequest(HttpServletRequest req) {

        final HttpManifest httpManifest = getContext().getManifestContext().getHttpManifest();

        final Supplier<HttpServletRequest> httpServletRequestSupplier;
        httpServletRequestSupplier = () -> req;

        final Function<HttpContent, Object> payloadDeserializerFunction;
        payloadDeserializerFunction = content -> deserialize(content, req);

        return new ServletHttpRequest(httpManifest, httpServletRequestSupplier, payloadDeserializerFunction);

    }

    @Override
    public HttpRequest getAsyncRequest(final AsyncContext asyncContext) {

        final HttpManifest httpManifest = getContext().getManifestContext().getHttpManifest();

        final Supplier<HttpServletRequest> httpServletRequestSupplier;
        httpServletRequestSupplier = () -> (HttpServletRequest)asyncContext.getRequest();

        final Function<HttpContent, Object> payloadDeserializerFunction;
        payloadDeserializerFunction = content -> deserialize(content, httpServletRequestSupplier.get());

        return new ServletHttpRequest(httpManifest, httpServletRequestSupplier, payloadDeserializerFunction);

    }

    private Object deserialize(final HttpContent httpContent, final HttpServletRequest req) {

        final String contentType = httpContent.getType();
        final Class<?> payloadClass = httpContent.getPayloadType();

        final PayloadReader payloadReader = getPayloadReadersByContentType().get(contentType);

        if (payloadReader == null) {
            throw new UnacceptableContentException("no reader configured for " + contentType);
        }

        try {
            return payloadReader.read(payloadClass, req.getInputStream());
        } catch (IOException ex) {
            throw new InternalException(ex);
        }

    }

    public Context getContext() {
        return context;
    }

    @Inject
    public void setContext(@Named(REMOTE) Context context) {
        this.context = context;
    }

    public Map<String, PayloadReader> getPayloadReadersByContentType() {
        return payloadReadersByContentType;
    }

    @Inject
    public void setPayloadReadersByContentType(Map<String, PayloadReader> payloadReadersByContentType) {
        this.payloadReadersByContentType = payloadReadersByContentType;
    }

}
