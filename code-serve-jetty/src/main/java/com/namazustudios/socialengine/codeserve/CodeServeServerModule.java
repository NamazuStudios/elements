package com.namazustudios.socialengine.codeserve;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.jetty.ServletContextHandlerProvider;
import com.namazustudios.socialengine.jetty.SimpleServerProvider;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class CodeServeServerModule extends PrivateModule {
    @Override
    protected void configure() {
        expose(Server.class);
        expose(ServletContextHandler.class);
        bind(Server.class).toProvider(SimpleServerProvider.class);
        bind(ServletContextHandler.class).toProvider(ServletContextHandlerProvider.class);
    }
}
