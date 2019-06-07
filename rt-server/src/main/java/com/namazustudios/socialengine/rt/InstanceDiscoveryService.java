package com.namazustudios.socialengine.rt;

import com.google.common.net.HostAndPort;

import java.util.UUID;

public interface InstanceDiscoveryService extends Listenable<InstanceDiscoveryServiceListener> {

    default void start() {}

    default void stop() {}

    HostAndPort getHostAndPort(UUID instanceUuid);

    UUID getInstanceUuid(HostAndPort hostAndPort);
}
