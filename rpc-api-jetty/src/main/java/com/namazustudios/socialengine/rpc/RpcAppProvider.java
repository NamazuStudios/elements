package com.namazustudios.socialengine.rpc;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.namazustudios.socialengine.jrpc.JrpcModule;
import com.namazustudios.socialengine.jrpc.JsonRpcNetwork;
import com.namazustudios.socialengine.rpc.guice.RpcJerseyModule;
import com.namazustudios.socialengine.servlet.security.HealthServlet;
import com.namazustudios.socialengine.servlet.security.HttpServletCORSFilter;
import com.namazustudios.socialengine.servlet.security.HttpServletGlobalSecretHeaderFilter;
import com.namazustudios.socialengine.servlet.security.VersionServlet;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import static com.namazustudios.socialengine.Constants.HTTP_PATH_PREFIX;
import static com.namazustudios.socialengine.rpc.RpcResourceConfig.INJECTOR_ATTRIBUTE_NAME;
import static java.lang.String.format;
import static java.util.EnumSet.allOf;

public class RpcAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final String HEALTH_PREFIX_FORMAT = "%s/health";

    private static final String VERSION_PREFIX_FORMAT = "%s/version";

    private static final String NETWORK_PREFIX_FORMAT = "%s/net/%s";

    private String prefix;

    private Injector injector;

    private DeploymentManager deploymentManager;

    private final Map<App, Function<App, ContextHandler>> startupOps = new LinkedHashMap<>();

    @Override
    protected void doStart() {
        doStartBase();
        doStartJrpcNetworks();
    }

    private void doStartBase() {
        final var injector = getInjector().createChildInjector(
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(HealthServlet.class).asEagerSingleton();
                        bind(VersionServlet.class).asEagerSingleton();
                        bind(HttpServletCORSFilter.class).asEagerSingleton();
                        bind(HttpServletGlobalSecretHeaderFilter.class).asEagerSingleton();
                    }
                }
        );

        final var healthServlet = injector.getInstance(HealthServlet.class);
        final var versionServlet = injector.getInstance(VersionServlet.class);
        final var corsFilter = injector.getInstance(HttpServletCORSFilter.class);
        final var globalHeaderFilter = injector.getInstance(HttpServletGlobalSecretHeaderFilter.class);

        final var servletContextHandler = new ServletContextHandler();


    }

    private void doStartJrpcNetworks() {
        for (var jsonRpcNetwork : JsonRpcNetwork.values()) {

            final var prefix = format(NETWORK_PREFIX_FORMAT,
                    getPrefix(),
                    jsonRpcNetwork.getPrefix()
            ).replace("/{2,}", "/");

            final var app = new App(getDeploymentManager(), this, prefix);
            getDeploymentManager().addApp(app);
            startupOps.put(app, a -> createContextHandlerForNetwork(a, jsonRpcNetwork));

        }
    }

    private ContextHandler createContextHandlerForNetwork(final App app, final JsonRpcNetwork jsonRpcNetwork) {

        final var injector = getInjector().createChildInjector(
            new RpcJerseyModule(),
            new JrpcModule().withNetwork(jsonRpcNetwork)
        );

        final var guiceFilter = injector.getInstance(GuiceFilter.class);
        final var servletContextHandler = injector.getInstance(ServletContextHandler.class);
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));
        servletContextHandler.setAttribute(INJECTOR_ATTRIBUTE_NAME, injector);
        servletContextHandler.setContextPath(app.getOriginId());

        return servletContextHandler;

    }

    @Override
    public ContextHandler createContextHandler(final App app) throws Exception {
        return startupOps.get(app).apply(app);
    }

    @Override
    protected void doStop() throws Exception {
        startupOps.clear();
    }

    public String getPrefix() {
        return prefix;
    }

    @Inject
    public void setPrefix(@Named(HTTP_PATH_PREFIX) String prefix) {
        this.prefix =
            prefix == null ?         null :
            prefix.startsWith("/") ? this.prefix :
                                     "/" + this.prefix;
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
