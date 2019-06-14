package com.namazustudios.socialengine.rt;
import static com.namazustudios.socialengine.rt.Listenable.Listener;

import java.util.Set;
import java.util.UUID;

public interface InstanceConnectionMonitorServiceListener extends Listener {
    default void onInstancesConnected(Set<UUID> instanceUuids) {}
    default void onInstancesDisconnected(Set<UUID> instanceUuids) {}
}