package com.namazustudios.socialengine.rt;

import com.google.common.net.HostAndPort;
import com.namazustudios.socialengine.rt.remote.ConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Connection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.rt.Constants.*;
import static java.util.stream.Collectors.toList;

public class StaticInstanceDiscoveryService implements InstanceDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(StaticInstanceDiscoveryService.class);

    public static final String REMOTE_CONNECT_ADDRESSES = "com.namazustudios.socialengine.rt.StaticInstanceDiscoveryService.remoteConnectAddresses";

    private Set<String> remoteConnectAddresses;

    private ConnectionService connectionService;

    private final AtomicReference<List<ConnectionService.Connection>> connectionList = new AtomicReference<>();

    @Override
    public void start() {

        final List<ConnectionService.Connection> connections = getRemoteConnectAddresses()
            .stream()
            .map(getConnectionService()::connectToInstance)
            .collect(toList());
        if (this.connectionList.compareAndSet(null, connections)) {
            logger.info("Connected to {} ", connections);
        } else {
            disconnect(connections);
        }

    }

    @Override
    public void stop() {
        final List<ConnectionService.Connection> connections = connectionList.getAndSet(null);
        if (connections == null) throw new IllegalStateException("Not connected.");
        disconnect(connections);
    }

    private void disconnect(final List<ConnectionService.Connection> connections) {
        connections.forEach(c -> {
            try {
                c.disconnect();
            } catch (Exception ex) {
                logger.error("Could not disconnect from {}.", ex);
            }
        });
    }

    public ConnectionService getConnectionService() {
        return connectionService;
    }

    @Inject
    public void setConnectionService(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    public Set<String> getRemoteConnectAddresses() {
        return remoteConnectAddresses;
    }

    @Inject
    public void setRemoteConnectAddresses(@Named(REMOTE_CONNECT_ADDRESSES) Set<String> remoteConnectAddresses) {
        this.remoteConnectAddresses = remoteConnectAddresses;
    }

}
