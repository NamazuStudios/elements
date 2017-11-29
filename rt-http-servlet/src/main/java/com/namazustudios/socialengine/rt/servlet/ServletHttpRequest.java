package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.Attributes;
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Collections.list;

public class ServletHttpRequest implements HttpRequest {

    public static List<Object> objectList(final Enumeration<?> enumeration) {
        final List<Object> objectList = new ArrayList<>();
        while (enumeration.hasMoreElements()) objectList.add(enumeration.nextElement());
        return objectList;
    }

    private final ServletRequestHeader servletRequestHeader;

    private final ServletRequestAttributes servletRequestAttributes;

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
        this.servletRequestHeader = new ServletRequestHeader(compositeHttpManifestMetadata, httpServletRequestSupplier);
        this.servletRequestAttributes = new ServletRequestAttributes(httpServletRequestSupplier::get);
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
    public Attributes getAttributes() {
        return servletRequestAttributes;
    }

    @Override
    public Object getPayload() {
        return payloadValue.get();
    }

    @Override
    public List<String> getParameterNames() {
        return list(httpServletRequestSupplier.get().getHeaderNames());
    }

    @Override
    public List<Object> getParameters(String parameterName) {
        final HttpServletRequest httpServletRequest = httpServletRequestSupplier.get();
        final Enumeration<String> parameterNames = httpServletRequest.getParameterNames();
        return parameterNames != null && parameterNames.hasMoreElements() ? objectList(parameterNames) : null;
    }

    private Object deserializePayload() {
        final HttpContent requestContent = getManifestMetadata().getPreferredRequestContent();
        return payloadDeserializerFunction.apply(requestContent);
    }

}
