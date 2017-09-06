package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.RequestHeader;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.rt.http.HttpRequest;
import com.namazustudios.socialengine.rt.http.XHttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static java.util.Collections.list;

public class ServletRequestHeader implements RequestHeader {

    private final HttpRequest httpRequest;

    private final HttpServletRequest httpServletRequest;

    private static List<Object> objectList(final Enumeration<?> enumeration) {
        final List<Object> objectList = new ArrayList<>();
        while (enumeration.hasMoreElements()) objectList.add(enumeration.nextElement());
        return objectList;
    }

    public ServletRequestHeader(final HttpRequest httpRequest, final HttpServletRequest httpServletRequest) {
        this.httpRequest = httpRequest;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public List<String> getHeaderNames() {
        return list(httpServletRequest.getHeaderNames());
    }

    @Override
    public List<Object> getHeaders(final String name) {
        final Enumeration<String> headers = httpServletRequest.getHeaders(name);
        return headers != null && headers.hasMoreElements() ? objectList(headers) : null;
    }

    @Override
    public int getSequence() {
        try {
            final String header = httpServletRequest.getHeader(XHttpHeaders.RT_SEQUENCE);
            return header == null ? -1 : Integer.parseInt(header);
        } catch (NumberFormatException nfe) {
            throw new BadRequestException(nfe);
        }
    }

    @Override
    public String getPath() {
        final String requestUri = httpServletRequest.getRequestURI();
        final String contextPath = httpServletRequest.getContextPath();
        return requestUri.substring(contextPath.length());
    }

    @Override
    public String getMethod() {
        return httpServletRequest.getMethod();
    }

}
