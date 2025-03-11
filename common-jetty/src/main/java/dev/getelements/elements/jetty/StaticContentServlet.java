package dev.getelements.elements.jetty;

import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jetty.ee10.servlet.ResourceServlet;

/**
 * Works around an issue in the base {@link ResourceServlet} which seems to be incompatible with Guice.
 */
public class StaticContentServlet extends ResourceServlet {

    @Override
    protected String getEncodedPathInContext(final HttpServletRequest request, final boolean included) {
        return request.getPathInfo();
    }

}
