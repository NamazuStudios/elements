package dev.getelements.elements.jetty;

import dev.getelements.elements.Constants;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static dev.getelements.elements.Constants.HTTP_BIND_ADDRESS;

public class SimpleServerProvider implements Provider<Server> {

    private Provider<Integer> serverPortProvider;

    private Provider<String> serverBindAddressProvider;

    @Override
    public Server get() {
        final var server = new Server();
        final var connector = new ServerConnector(server);
        connector.setPort(getServerPortProvider().get());
        connector.setHost(getServerBindAddressProvider().get());
        server.setConnectors(new Connector[]{connector});
        return server;
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
