package dev.getelements.elements.jetty;

import dev.getelements.elements.sdk.model.Constants;
import dev.getelements.elements.servlet.security.HappyServlet;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import static dev.getelements.elements.sdk.model.Constants.HTTP_BIND_ADDRESS;

public class SimpleServerProvider implements Provider<Server> {

    private Provider<Handler> handlerProvider;

    private Provider<Integer> serverPortProvider;

    private Provider<String> serverBindAddressProvider;

    @Override
    public Server get() {

        final var server = new Server();
        final var connector = new ServerConnector(server);

        connector.setPort(getServerPortProvider().get());
        connector.setHost(getServerBindAddressProvider().get());
        server.setConnectors(new Connector[]{connector});

        final var handler = getHandlerProvider().get();
        server.setHandler(handler);

        final var defaultServletHandler = new ServletContextHandler();
        defaultServletHandler.setContextPath("/");
        defaultServletHandler.addServlet(HappyServlet.class, "/");
        server.setDefaultHandler(defaultServletHandler);

        return server;

    }

    public Provider<Handler> getHandlerProvider() {
        return handlerProvider;
    }

    @Inject
    public void setHandlerProvider(Provider<Handler> handlerProvider) {
        this.handlerProvider = handlerProvider;
    }

    public Provider<Integer> getServerPortProvider() {
        return serverPortProvider;
    }

    @Inject
    public void setServerPortProvider(@Named(Constants.HTTP_PORT) Provider<Integer> serverPortProvider) {
        this.serverPortProvider = serverPortProvider;
    }

    public Provider<String> getServerBindAddressProvider() {
        return serverBindAddressProvider;
    }

    @Inject
    public void setServerBindAddressProvider(@Named(HTTP_BIND_ADDRESS) Provider<String> serverBindAddressProvider) {
        this.serverBindAddressProvider = serverBindAddressProvider;
    }

}
