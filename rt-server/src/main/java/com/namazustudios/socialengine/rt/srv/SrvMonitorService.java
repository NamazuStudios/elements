package com.namazustudios.socialengine.rt.srv;

import com.namazustudios.socialengine.rt.Listenable;

import java.util.Set;
import java.util.function.Consumer;

public interface SrvMonitorService extends Listenable<SrvMonitorServiceListener> {
    /**
     * Begins monitoring for SRV records that match the given FQDN.
     *
     * @param fqdn the fully-qualified domain name, should be of the form `_service._proto.name.`.
     *
     * @return true if monitoring started successfully, false otherwise.
     */
    boolean start(final String fqdn);

    void stop();

    Set<SrvRecord> getSrvRecords();
}
