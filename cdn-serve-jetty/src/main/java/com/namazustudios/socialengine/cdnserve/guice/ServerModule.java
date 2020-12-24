package com.namazustudios.socialengine.cdnserve.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.cdnserve.CdnAppProvider;
import com.namazustudios.socialengine.cdnserve.JettyDeploymentService;
import com.namazustudios.socialengine.cdnserve.api.DeploymentService;
import com.namazustudios.socialengine.jetty.ServerProvider;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.server.Server;


public class ServerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AppProvider.class).to(CdnAppProvider.class);
        bind(Server.class).toProvider(ServerProvider.class).asEagerSingleton();
        bind(DeploymentService.class).to(JettyDeploymentService.class).asEagerSingleton();
    }
}
