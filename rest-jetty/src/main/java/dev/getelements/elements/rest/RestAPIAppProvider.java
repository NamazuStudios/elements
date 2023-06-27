package dev.getelements.elements.rest;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import dev.getelements.elements.guice.StandardServletRedissonServicesModule;
import dev.getelements.elements.guice.StandardServletSecurityModule;
import dev.getelements.elements.guice.StandardServletServicesModule;
import dev.getelements.elements.rest.guice.RestAPIJerseyModule;
import dev.getelements.elements.service.guice.NotificationServiceModule;
import dev.getelements.elements.servlet.HttpContextRoot;
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

import static dev.getelements.elements.rest.guice.RestAPIGuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME;

public class RestAPIAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final Logger logger = LoggerFactory.getLogger(RestAPIAppProvider.class);

    private static final String REST_API_CONTEXT_ROOT = "/api/rest";

    private Injector injector;

    private DeploymentManager deploymentManager;

    private HttpContextRoot httpContextRoot;

    @Override
    protected void doStart() {

        final var apiContextRoot = getHttpContextRoot().normalize(REST_API_CONTEXT_ROOT);
        logger.info("Running REST API at {}", apiContextRoot);

        final var app = new App(
                deploymentManager,
                this,
                apiContextRoot,
                buildRestApiContext(apiContextRoot)
        );

        deploymentManager.addApp(app);

    }

    private ContextHandler buildRestApiContext(final String apiContextRoot) {

        final var injector = getInjector().createChildInjector(
            new RestAPIJerseyModule(),
            new NotificationServiceModule(),
            new StandardServletSecurityModule(),
            new StandardServletServicesModule(),
            new StandardServletRedissonServicesModule()
        );

        final var servletContextHandler = new ServletContextHandler();
        servletContextHandler.setAttribute(INJECTOR_ATTRIBUTE_NAME, injector);

        final var guiceFilter = injector.getInstance(GuiceFilter.class);
        final var restDocRedirectFilter = injector.getInstance(RestDocRedirectFilter.class);

        servletContextHandler.setContextPath(apiContextRoot);
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", EnumSet.allOf(DispatcherType.class));
        servletContextHandler.addFilter(new FilterHolder(restDocRedirectFilter), "/*", EnumSet.allOf(DispatcherType.class));

        return servletContextHandler;

    }

    @Override
    public ContextHandler createContextHandler(final App app) throws Exception {
        throw new IllegalStateException("No context handler.");
    }

    @Override
    public void setDeploymentManager(final DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    public HttpContextRoot getHttpContextRoot() {
        return httpContextRoot;
    }

    @Inject
    public void setHttpContextRoot(HttpContextRoot httpContextRoot) {
        this.httpContextRoot = httpContextRoot;
    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

}
