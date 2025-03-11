package dev.getelements.elements.jetty;

import dev.getelements.elements.servlet.HttpContextRoot;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;

import static org.eclipse.jetty.ee10.servlet.ServletContextHandler.SESSIONS;

public class ServletContextHandlerProvider implements Provider<ServletContextHandler> {

    private HttpContextRoot httpContextRoot;

    @Override
    public ServletContextHandler get() {
        final var servletHandler = new ServletContextHandler(SESSIONS);
        final var httpPathPrefix = getHttpContextRoot().getHttpPathPrefix();
        servletHandler.setContextPath(httpPathPrefix);
        return servletHandler;
    }

    public HttpContextRoot getHttpContextRoot() {
        return httpContextRoot;
    }

    @Inject
    public void setHttpContextRoot(HttpContextRoot httpContextRoot) {
        this.httpContextRoot = httpContextRoot;
    }

}
