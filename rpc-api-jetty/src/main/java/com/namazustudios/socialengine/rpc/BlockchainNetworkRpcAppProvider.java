package com.namazustudios.socialengine.rpc;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.servlet.GuiceFilter;
import com.namazustudios.socialengine.guice.StandardServletRedissonServicesModule;
import com.namazustudios.socialengine.guice.StandardServletSecurityModule;
import com.namazustudios.socialengine.guice.StandardServletServicesModule;
import com.namazustudios.socialengine.jrpc.JsonRpcModule;
import com.namazustudios.socialengine.model.blockchain.BlockchainNetwork;
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

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.Constants.HTTP_PATH_PREFIX;
import static com.namazustudios.socialengine.rpc.RpcResourceConfig.INJECTOR_ATTRIBUTE_NAME;
import static com.namazustudios.socialengine.servlet.security.HttpPathUtils.normalize;
import static java.lang.String.format;
import static java.util.EnumSet.allOf;

public class BlockchainNetworkRpcAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final String NETWORK_PREFIX_FORMAT = "%s/net/%s";

    private String rootContext;

    private Injector injector;

    private DeploymentManager deploymentManager;

    @Override
    protected void doStart() {
        for (var jsonRpcNetwork : BlockchainNetwork.values()) {

            final var contextPath = normalize(format(
                    NETWORK_PREFIX_FORMAT,
                    getRootContext(),
                    jsonRpcNetwork.toString().toLowerCase()
            ));

            final var app = new App(
                    getDeploymentManager(),
                    this,
                    contextPath,
                    createContextHandlerForNetwork(contextPath, jsonRpcNetwork)
            );

            getDeploymentManager().addApp(app);

        }
    }

    private ContextHandler createContextHandlerForNetwork(
            final String contextPath,
            final BlockchainNetwork blockchainNetwork) {


        final var urls = getInjector().getInstance(
                Key.get(
                        String.class,
                        named(blockchainNetwork.urlsName())
                )
        );

        final var injector = getInjector().createChildInjector(
                new RpcJerseyModule(),
                new JsonRpcJacksonModule(),
                new StandardServletSecurityModule(),
                new StandardServletServicesModule(),
                new StandardServletRedissonServicesModule(),
                new JsonRpcModule()
                        .withNetwork(blockchainNetwork)
                        .withHttpRedirectProvider(urls)
                        .scanningScope(blockchainNetwork.protocol().toString())
        );

        final var guiceFilter = injector.getInstance(GuiceFilter.class);
        final var servletContextHandler = injector.getInstance(ServletContextHandler.class);
        servletContextHandler.setContextPath(contextPath);
        servletContextHandler.addFilter(new FilterHolder(guiceFilter), "/*", allOf(DispatcherType.class));
        servletContextHandler.setAttribute(INJECTOR_ATTRIBUTE_NAME, injector);

        return servletContextHandler;

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
