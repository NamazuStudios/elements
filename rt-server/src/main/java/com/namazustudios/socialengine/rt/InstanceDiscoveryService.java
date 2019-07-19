package com.namazustudios.socialengine.rt;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Tracks and discovers remote instance of instances that have announced themselves on the network.
 */
public interface InstanceDiscoveryService {

    default void start() {}

    default void stop() {}

    Subscription subscribeToDiscovery(Consumer<InstanceHostInfo> instanceHostInfoConsumer);

    Subscription subscribeToUndiscovery(Consumer<InstanceHostInfo> instanceHostInfoConsumer);

    /**
     * Gets the known remote hosts.
     *
     * @return a {@link Set<String>} containing the remote hosts
     */
    Collection<InstanceHostInfo> getRemoteConnections();

}
