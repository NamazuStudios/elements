package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.Listenable.Listener;
import com.namazustudios.socialengine.rt.SrvUniqueIdentifier;

public interface ApplicationNodeAddressRegistryListener extends Listener {
    default void onApplicationNodeAddressCreated(Object address, SrvUniqueIdentifier srvUniqueIdentifier) {}
    default void onApplicationNodeAddressUpdated(Object address, SrvUniqueIdentifier srvUniqueIdentifier) {}
    default void onApplicationNodeAddressDeleted(Object address, SrvUniqueIdentifier srvUniqueIdentifier) {}
}