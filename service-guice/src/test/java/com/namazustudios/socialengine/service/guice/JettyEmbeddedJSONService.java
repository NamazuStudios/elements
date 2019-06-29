package com.namazustudios.socialengine.service.guice;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.eclipse.jetty.servlet.ServletContextHandler.SESSIONS;

public class JettyEmbeddedJSONService {

    private static final Logger logger = LoggerFactory.getLogger(JettyEmbeddedJSONService.class);

    private Server server;

    public void start() throws Exception {

        if (server != null) throw new IllegalStateException();
        server = new Server(0);

        final ServletContextHandler context = new ServletContextHandler(SESSIONS);
        context.setContextPath("/");
        context.addServlet(TestServlet.class, "/*");

        server.setHandler(context);
        server.start();

    }

    public void stop() throws Exception {
        server.stop();
        server.join();
    }

    public String getUri() {
        return server.getURI().toString();
    }

}
