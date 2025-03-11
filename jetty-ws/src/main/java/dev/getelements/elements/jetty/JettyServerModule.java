package dev.getelements.elements.jetty;

import com.google.inject.PrivateModule;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

public class JettyServerModule extends PrivateModule {

    @Override
    protected void configure() {
        requireBinding(Handler.class);
        bind(Server.class).toProvider(SimpleServerProvider.class);
        expose(Server.class);
    }

}
