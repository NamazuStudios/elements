package dev.getelements.elements.docserve.guice;

import com.google.inject.AbstractModule;
import dev.getelements.elements.docserve.DocAppProvider;
import dev.getelements.elements.jetty.DynamicServerProvider;
import org.eclipse.jetty.deploy.AppProvider;
import org.eclipse.jetty.server.Server;

public class ServerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AppProvider.class).to(DocAppProvider.class);
        bind(Server.class).toProvider(DynamicServerProvider.class).asEagerSingleton();
    }

}
