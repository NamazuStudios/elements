package dev.getelements.elements.rpc;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.servlet.GuiceFilter;
import dev.getelements.elements.guice.StandardServletRedissonServicesModule;
import dev.getelements.elements.guice.StandardServletSecurityModule;
import dev.getelements.elements.guice.StandardServletServicesModule;
import dev.getelements.elements.jrpc.JsonRpcModule;
import dev.getelements.elements.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.rpc.guice.JsonRpcJacksonModule;
import dev.getelements.elements.rpc.guice.RpcJerseyModule;
import dev.getelements.elements.servlet.HttpContextRoot;
import org.eclipse.jetty.deploy.App;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

import javax.inject.Inject;
import javax.servlet.DispatcherType;
import java.util.stream.Stream;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rpc.RpcResourceConfig.INJECTOR_ATTRIBUTE_NAME;
import static dev.getelements.elements.rt.annotation.RemoteScope.ELEMENTS_JSON_RPC_PROTOCOL;
import static java.util.EnumSet.allOf;

public class BlockchainNetworkRpcAppProvider extends AbstractLifeCycle implements AppProvider {

    private static final String NETWORK_PREFIX_FORMAT = "/rpc/net/%s";

    private Injector injector;

    private DeploymentManager deploymentManager;

    private HttpContextRoot httpContextRoot;

    @Override
    protected void doStart() {
        Stream.of(BlockchainNetwork.values())
                .filter(p -> p.api().getApis().contains(ELEMENTS_JSON_RPC_PROTOCOL))
                .forEach(this::startJsonRpcNetwork);
    }

    private void startJsonRpcNetwork(final BlockchainNetwork jsonRpcNetwork) {

        final var contextPath = getHttpContextRoot().formatNormalized(
                NETWORK_PREFIX_FORMAT,
                jsonRpcNetwork.toString().toLowerCase()
        );

        final var app = new App(
                getDeploymentManager(),
                this,
                contextPath,
                createContextHandlerForNetwork(contextPath, jsonRpcNetwork)
        );

        getDeploymentManager().addApp(app);

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
                        .scanningScope(blockchainNetwork.api().toString())
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

    public HttpContextRoot getHttpContextRoot() {
        return httpContextRoot;
    }

    @Inject
    public void setHttpContextRoot(HttpContextRoot httpContextRoot) {
        this.httpContextRoot = httpContextRoot;
    }

}
