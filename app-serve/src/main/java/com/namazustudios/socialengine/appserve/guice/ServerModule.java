package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.appserve.DispatcherAppProvider;
import com.namazustudios.socialengine.appserve.provider.ServerProvider;
import org.eclipse.jetty.deploy.AppProvider;
import org.mortbay.jetty.Server;

public class ServerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AppProvider.class).to(DispatcherAppProvider.class);
        bind(Server.class).toProvider(ServerProvider.class).asEagerSingleton();
    }
}
