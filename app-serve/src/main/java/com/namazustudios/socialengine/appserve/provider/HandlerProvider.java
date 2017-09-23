//package com.namazustudios.socialengine.appserve.provider;
//
//import com.namazustudios.socialengine.appserve.DispatcherAppProvider;
//import org.eclipse.jetty.deploy.DeploymentManager;
//import org.eclipse.jetty.server.Handler;
//
//import javax.inject.Inject;
//import javax.inject.Provider;
//
//
//public class HandlerProvider implements Provider<Handler> {
//
//
//    private Provider<DeploymentManager> deploymentManagerProvider;
//
//    private Provider<DispatcherAppProvider> dispatcherAppProviderProvider;
//
//    @Override
//    public Handler get() {
//
//    }
//
//    public Provider<DeploymentManager> getDeploymentManagerProvider() {
//        return deploymentManagerProvider;
//    }
//
//    @Inject
//    public void setDeploymentManagerProvider(Provider<DeploymentManager> deploymentManagerProvider) {
//        this.deploymentManagerProvider = deploymentManagerProvider;
//    }
//
//    public Provider<DispatcherAppProvider> getDispatcherAppProviderProvider() {
//        return dispatcherAppProviderProvider;
//    }
//
//    @Inject
//    public void setDispatcherAppProviderProvider(Provider<DispatcherAppProvider> dispatcherAppProviderProvider) {
//        this.dispatcherAppProviderProvider = dispatcherAppProviderProvider;
//    }
//
//}
