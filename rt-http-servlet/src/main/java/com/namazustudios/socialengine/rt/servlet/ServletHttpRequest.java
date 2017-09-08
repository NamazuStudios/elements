package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.RequestHeader;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.rt.http.CompositeHttpManifestMetadata;
import com.namazustudios.socialengine.rt.http.HttpManifestMetadata;
import com.namazustudios.socialengine.rt.http.HttpRequest;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.http.HttpVerb;
import com.namazustudios.socialengine.rt.util.LazyValue;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Function;

public class ServletHttpRequest implements HttpRequest {

    private final HttpServletRequest httpServletRequest;

    private final ServletRequestHeader servletRequestHeader;

    private final CompositeHttpManifestMetadata compositeHttpManifestMetadata;

    private final Function<HttpContent, Object> payloadDeserializerFunction;

    private final LazyValue<Object> payloadValue = new LazyValue<>(this::deserializePayload);

    public ServletHttpRequest(final HttpServletRequest httpServletRequest,
                              final HttpManifest httpManifest,
                              final Function<HttpContent, Object> payloadDeserializerFunction) {
        this.httpServletRequest = httpServletRequest;
        this.servletRequestHeader = new ServletRequestHeader(this, httpServletRequest);
        this.compositeHttpManifestMetadata = new CompositeHttpManifestMetadata(this, httpManifest);
        this.payloadDeserializerFunction = payloadDeserializerFunction;
    }

    @Override
    public HttpVerb getVerb() {
        try {
            return HttpVerb.valueOf(httpServletRequest.getMethod());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex);
        }
    }

    @Override
    public HttpManifestMetadata getManifestMetadata() {
        return compositeHttpManifestMetadata;
    }

    @Override
    public RequestHeader getHeader() {
        return servletRequestHeader;
    }

    @Override
    public Object getPayload() {
        return payloadValue.get();
    }

    private Object deserializePayload() {
        final HttpContent requestContent = getManifestMetadata().getPreferredRequestContent();
        return payloadDeserializerFunction.apply(requestContent);
    }

}
