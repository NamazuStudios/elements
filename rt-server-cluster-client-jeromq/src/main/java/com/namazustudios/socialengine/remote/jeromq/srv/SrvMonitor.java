package com.namazustudios.socialengine.remote.jeromq.srv;

import java.util.Set;
import java.util.function.Consumer;

public interface SrvMonitor {

    /**
     * Begins monitoring for SRV records that match the given FQDN.
     *
     * @param fqdn the fully-qualified domain name, should be of the form `_service._proto.name.`.
     *
     * @return true if monitoring started successfully, false otherwise.
     */
    boolean start(final String fqdn);

    void stop();

    void registerOnCreatedSrvRecordListener(Consumer<SrvRecord> consumer);
    boolean unregisterOnCreatedSrvRecordListener(Consumer<SrvRecord> consumer);

    void registerOnUpdatedSrvRecordListener(Consumer<SrvRecord> consumer);
    boolean unregisterOnUpdatedSrvRecordListener(Consumer<SrvRecord> consumer);

    void registerOnDeletedSrvRecordListener(Consumer<SrvRecord> consumer);
    boolean unregisterOnDeletedSrvRecordListener(Consumer<SrvRecord> consumer);

    Set<SrvRecord> getSrvRecords();
    Set<Consumer<SrvRecord>> getSrvCreationListeners();
    Set<Consumer<SrvRecord>> getSrvUpdateListeners();
    Set<Consumer<SrvRecord>> getSrvDeletionListeners();

}
