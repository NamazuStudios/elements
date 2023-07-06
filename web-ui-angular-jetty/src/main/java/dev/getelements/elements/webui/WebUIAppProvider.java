package dev.getelements.elements.webui;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import dev.getelements.elements.servlet.HttpContextRoot;
import jnr.ffi.annotations.In;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class WebUIAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final Logger logger = LoggerFactory.getLogger(WebUIAppProvider.class);

    private static final String WEB_UI_CONTEXT_ROOT = "/admin";

    private Injector injector;

    private DeploymentManager deploymentManager;

    private HttpContextRoot httpContextRoot;

    public DeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    @Override
    protected void doStart() throws Exception {

        final var apiContextRoot = getHttpContextRoot().normalize(WEB_UI_CONTEXT_ROOT);
        logger.info("Running Web UI at {}", apiContextRoot);

        final var injector = getInjector().createChildInjector(new WebUIServletModule());

        final var servletContextHandler = new ServletContextHandler();
        final var guiceFilter = injector.getInstance(GuiceFilter.class);

        servletContextHandler.setContextPath(apiContextRoot);
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", EnumSet.allOf(DispatcherType.class));

        getDeploymentManager().addApp(new App(
                deploymentManager,
                this,
                apiContextRoot,
                servletContextHandler
        ));

    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void setDeploymentManager(DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    @Override
    public ContextHandler createContextHandler(App app) throws Exception {
        throw new IllegalStateException("No context handler for: " + app.getOriginId());
    }

    public HttpContextRoot getHttpContextRoot() {
        return httpContextRoot;
    }

    @Inject
    public void setHttpContextRoot(HttpContextRoot httpContextRoot) {
        this.httpContextRoot = httpContextRoot;
    }

}
