package com.namazustudios.socialengine.jetty;

import com.namazustudios.socialengine.Constants;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static com.namazustudios.socialengine.Constants.HTTP_BIND_ADDRESS;

public class DynamicServerProvider implements Provider<Server> {

    private SimpleServerProvider serverProvider;

    private Provider<AppProvider> appProviderProvider;

    private Provider<DeploymentManager> deploymentManagerProvider;

    @Override
    public Server get() {

        final var server = getServerProvider().get();
        final var dispatcherAppProvider = getAppProviderProvider().get();
        final var deploymentManager = getDeploymentManagerProvider().get();

        final HandlerCollection mainHandler = new HandlerCollection();
        mainHandler.addHandler(new RequestLogHandler());

        final ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
        deploymentManager.setContexts(contextHandlerCollection);
        mainHandler.addHandler(contextHandlerCollection);

        deploymentManager.addAppProvider(dispatcherAppProvider);
        server.addBean(deploymentManager);
        server.setHandler(mainHandler);
        return server;
    }

    public SimpleServerProvider getServerProvider() {
        return serverProvider;
    }

    @Inject
    public void setServerProvider(SimpleServerProvider serverProvider) {
        this.serverProvider = serverProvider;
    }

    public Provider<DeploymentManager> getDeploymentManagerProvider() {
        return deploymentManagerProvider;
    }

    @Inject
    public void setDeploymentManagerProvider(Provider<DeploymentManager> deploymentManagerProvider) {
        this.deploymentManagerProvider = deploymentManagerProvider;
    }

    public Provider<AppProvider> getAppProviderProvider() {
        return appProviderProvider;
    }

    @Inject
    public void setAppProviderProvider(Provider<AppProvider> appProviderProvider) {
        this.appProviderProvider = appProviderProvider;
    }

}
