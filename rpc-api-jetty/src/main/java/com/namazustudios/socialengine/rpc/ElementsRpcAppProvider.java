package com.namazustudios.socialengine.rpc;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.namazustudios.socialengine.guice.StandardServletRedissonServicesModule;
import com.namazustudios.socialengine.guice.StandardServletSecurityModule;
import com.namazustudios.socialengine.guice.StandardServletServicesModule;
import com.namazustudios.socialengine.jrpc.JsonRpcModule;
import com.namazustudios.socialengine.rpc.guice.JsonRpcJacksonModule;
import com.namazustudios.socialengine.rpc.guice.RpcJerseyModule;
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

import static com.namazustudios.socialengine.Constants.HTTP_PATH_PREFIX;
import static com.namazustudios.socialengine.rpc.RpcResourceConfig.INJECTOR_ATTRIBUTE_NAME;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.API_SCOPE;
import static com.namazustudios.socialengine.servlet.security.HttpPathUtils.normalize;
import static java.lang.String.format;
import static java.util.EnumSet.allOf;

public class ElementsRpcAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final String ECI_PREFIX_FORMAT = "%s/rpc";

    private String rootContext;

    private Injector injector;

    private DeploymentManager deploymentManager;

    @Override
    protected void doStart() {

        final var contextPath = normalize(format(
                ECI_PREFIX_FORMAT,
                getRootContext()
        ));

        final var injector = getInjector().createChildInjector(
                new RpcJerseyModule(),
                new JsonRpcJacksonModule(),
                new StandardServletSecurityModule(),
                new StandardServletServicesModule(),
                new StandardServletRedissonServicesModule(),
                new JsonRpcModule()
                        .withNoRedirect()
                        .scanningScope(API_SCOPE)
        );

        final var guiceFilter = injector.getInstance(GuiceFilter.class);
        final var servletContextHandler = injector.getInstance(ServletContextHandler.class);

        servletContextHandler.setContextPath(contextPath);
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));
        servletContextHandler.setAttribute(INJECTOR_ATTRIBUTE_NAME, injector);

        final var app = new App(
                getDeploymentManager(),
                this,
                contextPath,
                servletContextHandler
        );

        getDeploymentManager().addApp(app);

    }

    @Override
    public ContextHandler createContextHandler(final App app) {
        return null;
    }

    public String getRootContext() {
        return rootContext;
    }

    @Inject
    public void setRootContext(@Named(HTTP_PATH_PREFIX) String rootContext) {
        this.rootContext = rootContext;
    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    public DeploymentManager getDeploymentManager() {
        return deploymentManager;
    }

    @Override
    public void setDeploymentManager(final DeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

}
