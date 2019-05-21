package com.namazustudios.socialengine.rt.srv;
import com.namazustudios.socialengine.rt.Listenable;

public interface SrvMonitorServiceListener extends Listenable.Listener {
    default void onSrvRecordCreated(SrvRecord srvRecord) {}
    default void onSrvRecordUpdated(SrvRecord srvRecord) {}
    default void onSrvRecordDeleted(SrvRecord srvRecord) {}
}