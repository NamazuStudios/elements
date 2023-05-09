package dev.getelements.elements.rest;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import dev.getelements.elements.guice.StandardServletSecurityModule;
import dev.getelements.elements.guice.StandardServletRedissonServicesModule;
import dev.getelements.elements.guice.StandardServletServicesModule;
import dev.getelements.elements.rest.guice.RestAPIJerseyModule;
import dev.getelements.elements.service.guice.NotificationServiceModule;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.DispatcherType;

import java.util.EnumSet;

import static dev.getelements.elements.Constants.API_PREFIX;
import static dev.getelements.elements.Constants.HTTP_PATH_PREFIX;
import static dev.getelements.elements.rest.guice.GuiceResourceConfig.INJECTOR_ATTRIBUTE_NAME;
import static dev.getelements.elements.servlet.security.HttpPathUtils.normalize;
import static java.lang.String.format;

public class RestAPIAppProvider extends AbstractLifeCycle implements AppProvider {

    private String rootContext;

    private String apiContext;

    private Injector injector;

    private DeploymentManager deploymentManager;

    @Override
    protected void doStart() throws Exception {
        final var apiContextRoot = normalize(format("%s/%s", getRootContext(), getApiContext()));
        deploymentManager.addApp(new App(deploymentManager, this, apiContextRoot, buildRestApiContext(apiContextRoot)));
    }

    private ContextHandler buildRestApiContext(final String apiContextRoot) {

        final var injector = this.injector.createChildInjector(
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

    public String getRootContext() {
        return rootContext;
    }

    @Inject
    public void setRootContext(@Named(HTTP_PATH_PREFIX) String rootContext) {
        this.rootContext = rootContext;
    }

    public String getApiContext() {
        return apiContext;
    }

    @Inject
    public void setApiContext(@Named(API_PREFIX) String apiContext) {
        this.apiContext = apiContext;
    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

}
