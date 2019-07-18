package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.remote.InstanceConnectionService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Set;

import static com.namazustudios.socialengine.rt.Constants.SRV_INSTANCE_CONTROL_PORT_NAME;
import static com.namazustudios.socialengine.rt.Constants.SRV_INSTANCE_INVOKER_PORT_NAME;

public class SpotifySrvInstanceDiscoveryService implements InstanceDiscoveryService {

    private Integer srvInstanceInvokerPort;

    private Integer srvInstanceControlPort;

    private InstanceConnectionService connectionService;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Set<String> getRemoteConnections() {
        return null;
    }

    public InstanceConnectionService getConnectionService() {
        return connectionService;
    }

    @Inject
    public void setConnectionService(InstanceConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    public Integer getSrvInstanceInvokerPort() {
        return srvInstanceInvokerPort;
    }

    @Inject
    @Named(SRV_INSTANCE_INVOKER_PORT_NAME)
    public void setSrvInstanceInvokerPort(Integer srvInstanceInvokerPort) {
        this.srvInstanceInvokerPort = srvInstanceInvokerPort;
    }

    public Integer getSrvInstanceControlPort() {
        return srvInstanceControlPort;
    }

    @Inject
    @Named(SRV_INSTANCE_CONTROL_PORT_NAME)
    public void setSrvInstanceControlPort(Integer srvInstanceControlPort) {
        this.srvInstanceControlPort = srvInstanceControlPort;
    }

}
