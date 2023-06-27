package dev.getelements.elements.rt.servlet;

import dev.getelements.elements.rt.RequestHeader;
import dev.getelements.elements.rt.exception.BadRequestException;
import dev.getelements.elements.rt.http.CompositeHttpManifestMetadata;
import dev.getelements.elements.rt.http.HttpManifestMetadata;
import dev.getelements.elements.rt.http.XHttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static java.util.Collections.list;

public class ServletRequestHeader implements RequestHeader {


    public static List<Object> objectList(final Enumeration<?> enumeration) {
        final List<Object> objectList = new ArrayList<>();
        while (enumeration.hasMoreElements()) objectList.add(enumeration.nextElement());
        return objectList;
    }

    private final HttpManifestMetadata httpManifestMetadata;

    private final String path;

    private final Supplier<HttpServletRequest> httpServletRequestSupplier;

    public ServletRequestHeader(final HttpManifestMetadata httpManifestMetadata,
                                final Supplier<HttpServletRequest> httpServletRequestSupplier) {
        final HttpServletRequest httpServletRequest = httpServletRequestSupplier.get();
        final String requestUri = httpServletRequest.getRequestURI();
        final String contextPath = httpServletRequest.getContextPath();
        this.httpManifestMetadata = httpManifestMetadata;
        this.path = requestUri.substring(contextPath.length());
        this.httpServletRequestSupplier = httpServletRequestSupplier;
    }

    @Override
    public List<String> getHeaderNames() {
        return list(httpServletRequestSupplier.get().getHeaderNames());
    }

    @Override
    public Optional<List<Object>> getHeaders(final String name) {
        final Enumeration<String> headers = httpServletRequestSupplier.get().getHeaders(name);
        return headers != null && headers.hasMoreElements() ? Optional.of(objectList(headers)) : Optional.empty();
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

    @Override
    public Map<String, String> getPathParameters() {
        return httpManifestMetadata
            .getPreferredOperation()
            .getPath()
            .extract(getParsedPath());
    }

}
