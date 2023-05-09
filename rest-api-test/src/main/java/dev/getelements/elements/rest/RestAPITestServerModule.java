package dev.getelements.elements.rest;

import com.google.inject.PrivateModule;
import dev.getelements.elements.jetty.ServletContextHandlerProvider;
import dev.getelements.elements.jetty.SimpleServerProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class RestAPITestServerModule extends PrivateModule {
    @Override
    protected void configure() {
        expose(Server.class);
        expose(ServletContextHandler.class);
        bind(Server.class).toProvider(SimpleServerProvider.class);
        bind(ServletContextHandler.class).toProvider(ServletContextHandlerProvider.class);
    }
}
