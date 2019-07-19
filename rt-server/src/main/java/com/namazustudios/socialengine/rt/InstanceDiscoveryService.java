package com.namazustudios.socialengine.rt;

import java.util.Set;

/**
 * Tracks and discovers remote instance of instances that have announced themselves on the network.
 */
public interface InstanceDiscoveryService {

    default void start() {}

    default void stop() {}

    /**
     * Gets the known remote hosts.
     *
     * @return a {@link Set<String>} containing the remote hosts
     */
    Set<InstanceHostInfo> getRemoteConnections();

}
