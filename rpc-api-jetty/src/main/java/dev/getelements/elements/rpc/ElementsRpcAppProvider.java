package dev.getelements.elements.rpc;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import dev.getelements.elements.guice.StandardServletRedissonServicesModule;
import dev.getelements.elements.guice.StandardServletSecurityModule;
import dev.getelements.elements.guice.StandardServletServicesModule;
import dev.getelements.elements.jrpc.JsonRpcModule;
import dev.getelements.elements.rpc.guice.JsonRpcJacksonModule;
import dev.getelements.elements.rpc.guice.RpcJerseyModule;
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

import static dev.getelements.elements.Constants.HTTP_PATH_PREFIX;
import static dev.getelements.elements.rpc.RpcResourceConfig.INJECTOR_ATTRIBUTE_NAME;
import static dev.getelements.elements.rt.annotation.RemoteScope.API_SCOPE;
import static dev.getelements.elements.servlet.security.HttpPathUtils.normalize;
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
