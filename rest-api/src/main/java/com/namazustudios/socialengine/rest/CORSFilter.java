package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Created by patricktwohig on 5/11/17.
 */
@Provider
public class CORSFilter implements ContainerResponseFilter {

    public static final Logger logger = LoggerFactory.getLogger(CORSFilter.class);

    private Set<URI> allowedOrigins;

    @Override
    public void filter(final ContainerRequestContext requestContext,
                       final ContainerResponseContext responseContext) throws IOException {

        final var originHeader = requestContext.getHeaderString(Headers.ORIGIN);

        if (originHeader == null) {
            return;
        }

        final URI origin;

        try {
            origin = new URI(originHeader);
        } catch (URISyntaxException e) {
            logger.info("Caught bad Origin header {}", originHeader, e);
            return;
        }

        if (isWildcard() || getAllowedOrigins().contains(origin)) {
            responseContext.getHeaders().add(Headers.AC_ALLOW_ORIGIN, originHeader);
            responseContext.getHeaders().add(Headers.AC_ALLOW_HEADERS, Headers.AC_ALLOW_HEADERS_VALUE);
            responseContext.getHeaders().add(Headers.AC_ALLOW_CREDENTIALS, Headers.AC_ALLOW_CREDENTIALS_VALUE);
            responseContext.getHeaders().add(Headers.AC_ALLOW_ALLOW_METHODS, Headers.AC_ALLOW_ALLOW_METHODS_VALUE);
        }

    }

    private boolean isWildcard() {
        return getAllowedOrigins().stream().anyMatch(origin -> "*".equals(origin.toString()));
    }

    public Set<URI> getAllowedOrigins() {
        return allowedOrigins;
    }

    @Inject
    public void setAllowedOrigins(@Named(Constants.CORS_ALLOWED_ORIGINS) Set<URI> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

}
