package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.RequestHeader;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.rt.http.CompositeHttpManifestMetadata;
import com.namazustudios.socialengine.rt.http.HttpManifestMetadata;
import com.namazustudios.socialengine.rt.http.HttpRequest;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.http.HttpVerb;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Supplier;

public class ServletHttpRequest implements HttpRequest {

    private final HttpServletRequest httpServletRequest;

    private final ServletRequestHeader servletRequestHeader;

    private final CompositeHttpManifestMetadata compositeHttpManifestMetadata;

    public ServletHttpRequest(final HttpServletRequest httpServletRequest,
                              final Supplier<HttpManifest> httpManifestSupplier) {
        this.httpServletRequest = httpServletRequest;
        this.servletRequestHeader = new ServletRequestHeader(this, httpServletRequest);
        this.compositeHttpManifestMetadata = new CompositeHttpManifestMetadata(() -> this, httpManifestSupplier);
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
        // TODO Need to implement full content negotiation for response types
        // as they are needed.  Likely need to defer to the manifest metadata
        return null;
    }

    @Override
    public <T> T getPayload(Class<T> cls) {
        // TODO Need to implement full content negotiation for response types
        // as they are needed.  Likely need to defer to the manifest metadata
        return null;
    }

}
