package com.namazustudios.socialengine.servlet.security;

import com.namazustudios.socialengine.Constants;
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
import java.util.Set;

import static com.namazustudios.socialengine.Headers.*;

public class HttpServletCORSFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletCORSFilter.class);

    private Set<URI> allowedOrigins;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(final ServletRequest servletRequest,
                         final ServletResponse servletResponse,
                         final FilterChain chain) throws IOException, ServletException {

        final var httpServletRequest = (HttpServletRequest) servletRequest;
        final var httpServletResponse = (HttpServletResponse) servletResponse;

        final var originHeader = httpServletRequest.getHeader(ORIGIN);

        if (originHeader == null) {

            final URI origin;

            try {
                origin = new URI(originHeader);
            } catch (URISyntaxException e) {
                logger.info("Caught bad Origin header {}", originHeader, e);
                chain.doFilter(servletRequest,servletResponse);
                return;
            }

            if (isWildcard() || getAllowedOrigins().contains(origin)) {
                httpServletResponse.addHeader(AC_ALLOW_ORIGIN, originHeader);
                httpServletResponse.addHeader(AC_ALLOW_HEADERS, AC_ALLOW_HEADERS_VALUE);
                httpServletResponse.addHeader(AC_ALLOW_CREDENTIALS, AC_ALLOW_CREDENTIALS_VALUE);
                httpServletResponse.addHeader(AC_ALLOW_ALLOW_METHODS, AC_ALLOW_ALLOW_METHODS_VALUE);
            }

        }

        chain.doFilter(servletRequest, servletResponse);

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
    public void setAllowedOrigins(@Named(Constants.CORS_ALLOWED_ORIGINS) Set<URI> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

}
