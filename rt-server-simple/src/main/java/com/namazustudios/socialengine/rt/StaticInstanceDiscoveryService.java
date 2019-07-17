package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.remote.InstanceConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.toList;

public class StaticInstanceDiscoveryService implements InstanceDiscoveryService {

    private static final Logger logger = LoggerFactory.getLogger(StaticInstanceDiscoveryService.class);

    public static final String REMOTE_CONNECT_ADDRESSES = "com.namazustudios.socialengine.rt.StaticInstanceDiscoveryService.remoteConnectAddresses";

    private Set<String> remoteConnectAddresses;

    private InstanceConnectionService connectionService;

    private final AtomicReference<List<InstanceConnectionService.InstanceConnection>> connectionList = new AtomicReference<>();

    @Override
    public void start() {

        final List<InstanceConnectionService.InstanceConnection> connections = getRemoteConnectAddresses()
            .stream()
            .map(getConnectionService()::connect)
            .collect(toList());

        if (this.connectionList.compareAndSet(null, connections)) {
            logger.info("Connected to {} ", connections);
        } else {
            disconnect(connections);
        }

    }

    @Override
    public void stop() {
        final List<InstanceConnectionService.InstanceConnection> connections = connectionList.getAndSet(null);
        if (connections == null) throw new IllegalStateException("Not connected.");
        disconnect(connections);
    }

    private void disconnect(final List<InstanceConnectionService.InstanceConnection> connections) {
        connections.forEach(c -> {
            try {
                c.disconnect();
            } catch (Exception ex) {
                logger.error("Could not disconnect from {}.", ex);
            }
        });
    }

    public InstanceConnectionService getConnectionService() {
        return connectionService;
    }

    @Inject
    public void setConnectionService(InstanceConnectionService connectionService) {
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
