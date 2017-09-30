package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.RequestHeader;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.rt.http.HttpRequest;
import com.namazustudios.socialengine.rt.http.XHttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Collections.list;

public class ServletRequestHeader implements RequestHeader {

    private final String path;

    private final Supplier<HttpServletRequest> httpServletRequestSupplier;

    private static List<Object> objectList(final Enumeration<?> enumeration) {
        final List<Object> objectList = new ArrayList<>();
        while (enumeration.hasMoreElements()) objectList.add(enumeration.nextElement());
        return objectList;
    }

    public ServletRequestHeader(final Supplier<HttpServletRequest> httpServletRequestSupplier) {
        final HttpServletRequest httpServletRequest = httpServletRequestSupplier.get();
        final String requestUri = httpServletRequest.getRequestURI();
        final String contextPath = httpServletRequest.getContextPath();
        this.path = requestUri.substring(contextPath.length());
        this.httpServletRequestSupplier = httpServletRequestSupplier;
    }

    @Override
    public List<String> getHeaderNames() {
        return list(httpServletRequestSupplier.get().getHeaderNames());
    }

    @Override
    public List<Object> getHeaders(final String name) {
        final Enumeration<String> headers = httpServletRequestSupplier.get().getHeaders(name);
        return headers != null && headers.hasMoreElements() ? objectList(headers) : null;
    }

    @Override
    public int getSequence() {
        try {
            final String header = httpServletRequestSupplier.get().getHeader(XHttpHeaders.RT_SEQUENCE);
            return header == null ? -1 : Integer.parseInt(header);
        } catch (NumberFormatException nfe) {
            throw new BadRequestException(nfe);
        }
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getMethod() {
        return httpServletRequestSupplier.get().getMethod();
    }

}
