package com.namazustudios.socialengine.docserve.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.docserve.DocAppProvider;
import com.namazustudios.socialengine.jetty.DynamicServerProvider;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.server.Server;

public class ServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AppProvider.class).to(DocAppProvider.class);
        bind(Server.class).toProvider(DynamicServerProvider.class).asEagerSingleton();
    }

}
