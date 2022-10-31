package com.namazustudios.socialengine.servlet.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static com.namazustudios.socialengine.Constants.CORS_ALLOWED_ORIGINS;
import static com.namazustudios.socialengine.Constants.DOC_OUTSIDE_URL;
import static com.namazustudios.socialengine.Headers.*;
import static com.namazustudios.socialengine.util.URIs.originFor;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class HttpServletCORSFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletCORSFilter.class);

    private final Set<URI> allowedOrigins = new HashSet<>();

    public static final String INTERCEPT = "intercept";

    private ServletFilterProcessor<HttpServletRequest, HttpServletResponse> processor = this::proceedNormally;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {

        final var intercept = Boolean.parseBoolean(filterConfig.getInitParameter(INTERCEPT));

        if (intercept) {
            processor = this::proceedWithInterception;
        }

    }

    @Override
    public void doFilter(final ServletRequest servletRequest,
                         final ServletResponse servletResponse,
                         final FilterChain chain) throws IOException, ServletException {

        final var httpServletRequest = (HttpServletRequest) servletRequest;
        final var httpServletResponse = (HttpServletResponse) servletResponse;

        final var originHeader = httpServletRequest.getHeader(ORIGIN);

        if (originHeader != null) {

            final URI origin;

            try {
                origin = new URI(originHeader);
            } catch (URISyntaxException e) {
                logger.info("Caught bad Origin header {}", originHeader, e);
                chain.doFilter(servletRequest, servletResponse);
                return;
            }

            if (isWildcard() || getAllowedOrigins().contains(origin)) {
                httpServletResponse.addHeader(AC_ALLOW_ORIGIN, originHeader);
                httpServletResponse.addHeader(AC_ALLOW_HEADERS, AC_ALLOW_HEADERS_VALUE);
                httpServletResponse.addHeader(AC_ALLOW_CREDENTIALS, AC_ALLOW_CREDENTIALS_VALUE);
                httpServletResponse.addHeader(AC_ALLOW_ALLOW_METHODS, AC_ALLOW_ALLOW_METHODS_VALUE);
            }

        }

        processor.process(httpServletRequest, httpServletResponse, chain);

    }

    private void proceedNormally(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final FilterChain chain) throws IOException, ServletException {
        chain.doFilter(httpServletRequest, httpServletResponse);
    }

    private void proceedWithInterception(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final FilterChain filterChain) {
        httpServletResponse.setStatus(SC_OK);
    }

    private boolean isWildcard() {
        return getAllowedOrigins().stream().anyMatch(origin -> "*".equals(origin.toString()));
    }

    @Override
    public void destroy() {}

    public Set<URI> getAllowedOrigins() {
        return allowedOrigins;
    }

    @Inject
    public void addDocServeOrigins(@Named(DOC_OUTSIDE_URL) final URI docServeRoot) throws URISyntaxException {
        final var origin = originFor(docServeRoot);
        this.allowedOrigins.add(origin);
    }

    @Inject
    public void addAllowedOrigins(@Named(CORS_ALLOWED_ORIGINS) final Set<URI> allowedOrigins) {
        this.allowedOrigins.addAll(allowedOrigins);
    }

}
