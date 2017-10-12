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
import java.util.function.Supplier;

public class ServletHttpRequest implements HttpRequest {


    private final ServletRequestHeader servletRequestHeader;

    private final CompositeHttpManifestMetadata compositeHttpManifestMetadata;

    private final Supplier<HttpServletRequest> httpServletRequestSupplier;

    private final Function<HttpContent, Object> payloadDeserializerFunction;

    private final LazyValue<Object> payloadValue = new LazyValue<>(this::deserializePayload);

    public ServletHttpRequest(final HttpManifest httpManifest,
                              final Supplier<HttpServletRequest> httpServletRequestSupplier,
                              final Function<HttpContent, Object> payloadDeserializerFunction) {
        this.httpServletRequestSupplier = httpServletRequestSupplier;
        this.payloadDeserializerFunction = payloadDeserializerFunction;
        this.compositeHttpManifestMetadata = new CompositeHttpManifestMetadata(this, httpManifest);
        this.servletRequestHeader = new ServletRequestHeader(httpServletRequestSupplier);
    }

    @Override
    public HttpVerb getVerb() {
        try {
            return HttpVerb.valueOf(httpServletRequestSupplier.get().getMethod());
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
