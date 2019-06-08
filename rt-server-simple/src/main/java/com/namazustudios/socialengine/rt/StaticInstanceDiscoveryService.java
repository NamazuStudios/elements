package com.namazustudios.socialengine.rt;

import com.google.common.net.HostAndPort;
import com.namazustudios.socialengine.rt.remote.ConnectionService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.rt.Constants.BIND_PORT_NAME;
import static com.namazustudios.socialengine.rt.Constants.LOCAL_INSTANCE_CONNECT_PORTS_NAME;

public class StaticInstanceDiscoveryService implements InstanceDiscoveryService {
    ConnectionService connectionService;

    Set<Integer> localInstanceConnectPorts;
    Integer bindPort;

    @Override
    public void start() {
        // do not connect to the local instance
        final Set<Integer> connectPorts = new HashSet<>(localInstanceConnectPorts);
        connectPorts.remove(bindPort);

        final Set<HostAndPort> hostsAndPorts = connectPorts
                .stream()
                .map(port -> HostAndPort.fromParts("localhost", port))
                .collect(Collectors.toSet());
        hostsAndPorts.forEach(hostAndPort -> getConnectionService().connectToBackend(hostAndPort));
    }

    @Override
    public void stop() {
        // do not disconnect from the local instance since the conn does not exist
        final Set<Integer> connectPorts = new HashSet<>(localInstanceConnectPorts);
        connectPorts.remove(bindPort);

        final Set<HostAndPort> hostsAndPorts = connectPorts
                .stream()
                .map(port -> HostAndPort.fromParts("localhost", port))
                .collect(Collectors.toSet());
        hostsAndPorts.forEach(hostAndPort -> getConnectionService().disconnectFromBackend(hostAndPort));
    }

    public ConnectionService getConnectionService() {
        return connectionService;
    }

    @Inject
    public void setConnectionService(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    public Set<Integer> getLocalInstanceConnectPorts() {
        return localInstanceConnectPorts;
    }

    @Inject
    @Named(LOCAL_INSTANCE_CONNECT_PORTS_NAME)
    public void setLocalInstanceConnectPorts(Set<Integer> localInstanceConnectPorts) {
        this.localInstanceConnectPorts = localInstanceConnectPorts;
    }

    public Integer getBindPort() {
        return bindPort;
    }

    @Inject
    @Named(BIND_PORT_NAME)
    public void setBindPort(Integer bindPort) {
        this.bindPort = bindPort;
    }
}
