package com.namazustudios.socialengine.appserve.provider;

import com.namazustudios.socialengine.Constants;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.deploy.DeploymentManager;
import org.mortbay.jetty.Server;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

public class ServerProvider implements Provider<Server> {

    private Provider<Integer> serverPortProvider;

    private Provider<AppProvider> appProviderProvider;

    private Provider<DeploymentManager> deploymentManagerProvider;


    @Override
    public Server get() {
        final int port = getServerPortProvider().get();
        final Server server = new Server(port);
        final AppProvider dispatcherAppProvider = getAppProviderProvider().get();
        final DeploymentManager deploymentManager = getDeploymentManagerProvider().get();
        deploymentManager.addAppProvider(dispatcherAppProvider);
        server.getContainer().addBean(deploymentManager);
        return server;
    }

    public Provider<Integer> getServerPortProvider() {
        return serverPortProvider;
    }

    @Inject
    public void setServerPortProvider(@Named(Constants.HTTP_TUNNEL_PORT) Provider<Integer> serverPortProvider) {
        this.serverPortProvider = serverPortProvider;
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
