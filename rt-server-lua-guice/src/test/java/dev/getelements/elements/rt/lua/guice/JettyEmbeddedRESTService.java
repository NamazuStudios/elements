package dev.getelements.elements.rt.lua.guice;

import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.rt.jersey.GenericMultipartFeature;
import dev.getelements.elements.rt.lua.guice.rest.SimpleModelEndpoint;
import dev.getelements.elements.rt.lua.guice.rest._t;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletContext;

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
        servlet.setInitParameter("javax.ws.rs.Application", EmbeddedResourceConfig.class.getName());

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

    public static class EmbeddedResourceConfig extends ResourceConfig {
        public EmbeddedResourceConfig() {
            register(JacksonFeature.class);
            register(MultiPartFeature.class);
            register(GenericMultipartFeature.class);
            packages(_t.class.getPackage().getName());
        }
    }

    public static void main(final String[] args) throws InterruptedException {

        final var service = new JettyEmbeddedRESTService();
        service.start();

        logger.info("Server running at {}", service.getUri());
        service.server.join();

    }

}
