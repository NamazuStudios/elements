package com.namazustudios.socialengine.rest;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.namazustudios.socialengine.rest.guice.RestAPIRedissonServicesModule;
import com.namazustudios.socialengine.rest.guice.RestAPISecurityModule;
import com.namazustudios.socialengine.rest.guice.RestAPIServicesModule;
import com.namazustudios.socialengine.rest.guice.RestAPIJerseyModule;
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

import static com.namazustudios.socialengine.Constants.API_PREFIX;
import static com.namazustudios.socialengine.Constants.HTTP_PATH_PREFIX;
import static com.namazustudios.socialengine.servlet.security.HttpPathUtils.normalize;
import static java.lang.String.format;

public class RestAppProvider extends AbstractLifeCycle implements AppProvider {

    private String rootContext;

    private String apiContext;

    private Injector injector;

    private DeploymentManager deploymentManager;

    @Override
    protected void doStart() throws Exception {
        final var apiContextRoot = normalize(format("%s/%s/*", getRootContext(), getApiContext()));
        deploymentManager.addApp(new App(deploymentManager, this, apiContextRoot, buildRestApiContext(apiContextRoot)));
    }

    private ContextHandler buildRestApiContext(final String apiContextRoot) {

        final var injector = this.injector.createChildInjector(
            new RestAPIJerseyModule(),
            new RestAPISecurityModule(),
            new RestAPIServicesModule(),
            new RestAPIRedissonServicesModule()
        );

        final var servletContextHandler = new ServletContextHandler();
        final var guiceFilter = injector.getInstance(GuiceFilter.class);
        final var restDocRedirectFilter = injector.getInstance(RestDocRedirectFilter.class);
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), apiContextRoot, EnumSet.allOf(DispatcherType.class));
        servletContextHandler.addFilter(new FilterHolder(restDocRedirectFilter), apiContextRoot, EnumSet.allOf(DispatcherType.class));

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
