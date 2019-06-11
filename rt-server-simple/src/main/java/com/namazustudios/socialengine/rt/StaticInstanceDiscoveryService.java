package com.namazustudios.socialengine.rt;

import com.google.common.net.HostAndPort;
import com.namazustudios.socialengine.rt.remote.ConnectionService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static com.namazustudios.socialengine.rt.Constants.*;

public class StaticInstanceDiscoveryService implements InstanceDiscoveryService {
    ConnectionService connectionService;

    List<String> staticInstanceInvokerAddresses;
    List<String> staticInstanceControlAddresses;

    Integer currentInstanceInvokerPort;
    Integer currentInstanceControlPort;

    @Override
    public void start() {
        if (getStaticInstanceInvokerAddresses().size() != getStaticInstanceControlAddresses().size()) {
            throw new IllegalStateException("Static Instance Invoker Addresses size must match Static Instance Control Addresses size.");
        }

        for (int i=0; i<getStaticInstanceInvokerAddresses().size(); i++) {
            final String invokerAddress = getStaticInstanceInvokerAddresses().get(i);
            final String controlAddress = getStaticInstanceControlAddresses().get(i);

            final HostAndPort invokerHostAndPort = HostAndPort.fromString(invokerAddress);
            final HostAndPort controlHostAndPort = HostAndPort.fromString(controlAddress);
            getConnectionService().connectToInstance(invokerHostAndPort, controlHostAndPort);
        }
    }

    @Override
    public void stop() {
        for (int i=0; i<getStaticInstanceInvokerAddresses().size(); i++) {
            final String invokerAddress = getStaticInstanceInvokerAddresses().get(i);
            final HostAndPort invokerHostAndPort = HostAndPort.fromString(invokerAddress);
            getConnectionService().disconnectFromInstance(invokerHostAndPort);
        }
    }

    public ConnectionService getConnectionService() {
        return connectionService;
    }

    @Inject
    public void setConnectionService(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    public List<String> getStaticInstanceInvokerAddresses() {
        return staticInstanceInvokerAddresses;
    }

    @Inject
    @Named(STATIC_INSTANCE_INVOKER_ADDRESSES_NAME)
    public void setStaticInstanceInvokerAddresses(List<String> staticInstanceInvokerAddresses) {
        this.staticInstanceInvokerAddresses = staticInstanceInvokerAddresses;
    }

    public List<String> getStaticInstanceControlAddresses() {
        return staticInstanceControlAddresses;
    }

    @Inject
    @Named(STATIC_INSTANCE_CONTROL_ADDRESSES_NAME)
    public void setStaticInstanceControlAddresses(List<String> staticInstanceControlAddresses) {
        this.staticInstanceControlAddresses = staticInstanceControlAddresses;
    }

    public Integer getCurrentInstanceInvokerPort() {
        return currentInstanceInvokerPort;
    }

    @Inject
    @Named(CURRENT_INSTANCE_INVOKER_PORT_NAME)
    public void setCurrentInstanceInvokerPort(Integer currentInstanceInvokerPort) {
        this.currentInstanceInvokerPort = currentInstanceInvokerPort;
    }

    public Integer getCurrentInstanceControlPort() {
        return currentInstanceControlPort;
    }

    @Inject
    @Named(CURRENT_INSTANCE_CONTROL_PORT_NAME)
    public void setCurrentInstanceControlPort(Integer currentInstanceControlPort) {
        this.currentInstanceControlPort = currentInstanceControlPort;
    }
}
