package dev.getelements.elements.jetty;

import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static dev.getelements.elements.Constants.HTTP_PATH_PREFIX;
import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;

public class ServletContextHandlerProvider implements Provider<ServletContextHandler> {

    private Provider<String> apiContextRootProvider;

    @Override
    public ServletContextHandler get() {
        final var servletHandler = new ServletContextHandler(SESSIONS);
        final var root = getApiContextRootProvider().get();
        servletHandler.setContextPath(root.startsWith("/") ? root : "/" + root);
        return servletHandler;
    }

    public Provider<String> getApiContextRootProvider() {
        return apiContextRootProvider;
    }

    @Inject
    public void setApiContextRoot(@Named(HTTP_PATH_PREFIX) Provider<String> apiContextRootProvider) {
        this.apiContextRootProvider = apiContextRootProvider;
    }

}
