package com.namazustudios.socialengine.rt.remote;

import com.google.common.net.HostAndPort;
import com.namazustudios.socialengine.rt.Listenable.Listener;

public interface ApplicationNodeAddressRegistryListener extends Listener {
    default void onApplicationNodeAddressCreated(Object address, HostAndPort hostAndPort) {}
    default void onApplicationNodeAddressUpdated(Object address, HostAndPort hostAndPort) {}
    default void onApplicationNodeAddressDeleted(Object address, HostAndPort hostAndPort) {}
}