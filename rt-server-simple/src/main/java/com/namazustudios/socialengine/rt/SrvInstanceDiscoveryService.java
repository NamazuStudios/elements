package com.namazustudios.socialengine.rt;

import com.google.common.net.HostAndPort;
import com.namazustudios.socialengine.rt.remote.ConnectionService;
import com.namazustudios.socialengine.rt.srv.SrvMonitorServiceListener;
import com.namazustudios.socialengine.rt.srv.SrvRecord;

import javax.inject.Inject;
import javax.inject.Named;

import static com.namazustudios.socialengine.rt.Constants.SRV_INSTANCE_CONTROL_PORT_NAME;
import static com.namazustudios.socialengine.rt.Constants.SRV_INSTANCE_INVOKER_PORT_NAME;

public class SrvInstanceDiscoveryService implements InstanceDiscoveryService, SrvMonitorServiceListener {
    private ConnectionService connectionService;
    private Integer srvInstanceInvokerPort;
    private Integer srvInstanceControlPort;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    public void onSrvRecordCreated(SrvRecord srvRecord) {
        // TODO: for now, we should connect, look to see if the status response's instance uuid matches local instance
        //  uuid, and then disconnect if so and record the hostname.
        final HostAndPort invokerHostAndPort = HostAndPort.fromParts(srvRecord.getHost(), getSrvInstanceInvokerPort());
        final HostAndPort controlHostAndPort = HostAndPort.fromParts(srvRecord.getHost(), getSrvInstanceControlPort());
        getConnectionService().connectToInstance(invokerHostAndPort, controlHostAndPort);
    }

    public void onSrvRecordDeleted(SrvRecord srvRecord) {
        final HostAndPort invokerHostAndPort = HostAndPort.fromParts(srvRecord.getHost(), getSrvInstanceInvokerPort());
        final HostAndPort controlHostAndPort = HostAndPort.fromParts(srvRecord.getHost(), getSrvInstanceControlPort());
        getConnectionService().disconnectFromInstance(invokerHostAndPort, controlHostAndPort);
    }

    public ConnectionService getConnectionService() {
        return connectionService;
    }

    @Inject
    public void setConnectionService(ConnectionService connectionService) {
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
