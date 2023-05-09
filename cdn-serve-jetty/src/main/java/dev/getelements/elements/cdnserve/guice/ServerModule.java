package dev.getelements.elements.cdnserve.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.cdnserve.CdnAppProvider;
import dev.getelements.elements.jetty.DynamicServerProvider;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.server.Server;


public class ServerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(AppProvider.class).to(CdnAppProvider.class);
        bind(Server.class).toProvider(DynamicServerProvider.class).asEagerSingleton();
    }
}
