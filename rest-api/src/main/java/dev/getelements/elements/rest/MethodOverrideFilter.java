package dev.getelements.elements.rest;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Set;

import static dev.getelements.elements.rt.http.XHttpHeaders.X_HTTP_METHOD_OVERRIDE;
import static javax.ws.rs.HttpMethod.POST;

/**
 * Provides support for X-Http-Method-Override.
 */
@Provider
@PreMatching
public class MethodOverrideFilter implements ContainerRequestFilter {

    private static final Set<String> SUPPORTED_METHODS = Set.of("PUT", "PATCH");

    @Override
    public void filter(final ContainerRequestContext cxt) throws IOException {
        final var value = cxt.getHeaderString(X_HTTP_METHOD_OVERRIDE);
        if (POST.equals(cxt.getMethod()) && value != null && SUPPORTED_METHODS.contains(value))
            cxt.setMethod(value);
    }

}
