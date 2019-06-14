package com.namazustudios.socialengine.rt;

import java.util.Set;
import java.util.UUID;

public interface InstanceConnectionMonitorService extends Listenable<InstanceConnectionMonitorServiceListener> {
    default void start() {}
    default void stop() {}

    Set<UUID> getInstanceUuids();
}