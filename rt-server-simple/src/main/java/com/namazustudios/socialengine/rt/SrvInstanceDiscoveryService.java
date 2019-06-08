package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.remote.ConnectionService;
import com.namazustudios.socialengine.rt.srv.SrvMonitorServiceListener;
import com.namazustudios.socialengine.rt.srv.SrvRecord;

import javax.inject.Inject;

public class SrvInstanceDiscoveryService implements InstanceDiscoveryService, SrvMonitorServiceListener {
    ConnectionService connectionService;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    public void onSrvRecordCreated(SrvRecord srvRecord) {
        // TODO: for now, we should connect, look to see if the status response's instance uuid matches local instance
        //  uuid, and then disconnect if so and record the hostname. 
        getConnectionService().connectToBackend(srvRecord.getHostAndPort());
    }

    public void onSrvRecordDeleted(SrvRecord srvRecord) {
        getConnectionService().disconnectFromBackend(srvRecord.getHostAndPort());
    }

    public ConnectionService getConnectionService() {
        return connectionService;
    }

    @Inject
    public void setConnectionService(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }
}
