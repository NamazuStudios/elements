
package dev.getelements.elements.sdk.jakarta.rs;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Set;

import static jakarta.ws.rs.HttpMethod.POST;

/**
 * Provides support for X-Http-Method-Override.
 */
@Provider
@PreMatching
public class MethodOverrideFilter implements ContainerRequestFilter {

    /**
     * Used by "X-HTTP-Method-Override"
     */
    public static final String X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";
    private static final Set<String> SUPPORTED_METHODS = Set.of("PUT", "PATCH");

    @Override
    public void filter(final ContainerRequestContext cxt) throws IOException {
        final var value = cxt.getHeaderString(X_HTTP_METHOD_OVERRIDE);
        if (POST.equals(cxt.getMethod()) && value != null && SUPPORTED_METHODS.contains(value))
            cxt.setMethod(value);
    }

}
