package com.namazustudios.socialengine.rt.lua.guice;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.lua.guice.rest.SimpleModelEndpoint;
import com.namazustudios.socialengine.rt.lua.guice.rest._t;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;

public class JettyEmbeddedRESTService {

    private static final Logger logger = LoggerFactory.getLogger(JettyEmbeddedRESTService.class);

    private Server server;

    public JettyEmbeddedRESTService start() {

        if (server != null) throw new IllegalStateException();
        server = new Server(0);

        final ServletContextHandler context = new ServletContextHandler(SESSIONS);
        context.setContextPath("/");

        final ServletHolder servlet = context.addServlet(ServletContainer.class, "/*");

        servlet.setInitParameter(ServerProperties.PROVIDER_PACKAGES, _t.class.getPackage().getName());
        servlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, JacksonFeature.class.getName());

        server.setHandler(context);

        try {
            server.start();
        } catch (Exception e) {
            throw new InternalException(e);
        }

        return this;

    }

    public void stop() throws Exception {
        server.stop();
        server.join();
        SimpleModelEndpoint.clear();
    }

    public String getUri() {
        return server.getURI().toString();
    }

}
