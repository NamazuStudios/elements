package com.namazustudios.socialengine.rt;
import static com.namazustudios.socialengine.rt.Listenable.Listener;

import java.util.UUID;

public interface InstanceConnectionMonitorServiceListener extends Listener {
    default void onInstanceConnected(UUID instanceUuid) {}
    default void onInstanceDisconnected(UUID instanceUuid) {}
}