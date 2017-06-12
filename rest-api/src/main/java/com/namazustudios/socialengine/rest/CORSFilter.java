package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.Constants;
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

    public static final String ORIGIN = "Origin";

    public static final String AC_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

    public static final String AC_ALLOW_HEADERS = "Access-Control-Allow-Headers";

    public static final String AC_ALLOW_HEADERS_VALUE = "X-HTTP-Method-Override, Content-Type";

    public static final String AC_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

    public static final String AC_ALLOW_CREDENTIALS_VALUE = "true";

    public static final String AC_ALLOW_ALLOW_METHODS = "Access-Control-Allow-Methods";

    public static final String AC_ALLOW_ALLOW_METHODS_VALUE = "GET, POST, PUT, DELETE";

    private Set<URI> allowedOrigins;

    @Override
    public void filter(final ContainerRequestContext requestContext,
                       final ContainerResponseContext responseContext) throws IOException {

        final String originHeader = requestContext.getHeaderString(ORIGIN);

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

        if (getAllowedOrigins().contains(origin)) {
            responseContext.getHeaders().add(AC_ALLOW_ORIGIN, originHeader);
            responseContext.getHeaders().add(AC_ALLOW_HEADERS, AC_ALLOW_HEADERS_VALUE);
            responseContext.getHeaders().add(AC_ALLOW_CREDENTIALS, AC_ALLOW_CREDENTIALS_VALUE);
            responseContext.getHeaders().add(AC_ALLOW_ALLOW_METHODS, AC_ALLOW_ALLOW_METHODS_VALUE);
        }

    }

    public Set<URI> getAllowedOrigins() {
        return allowedOrigins;
    }

    @Inject
    public void setAllowedOrigins(@Named(Constants.CORS_ALLOWED_ORIGINS) Set<URI> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

}
