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

    /**
     * Gets the known remote hosts.
     *
     * @return a {@link Set<String>} containing the remote hosts
     */
    Collection<InstanceHostInfo> getKnownHosts();

    /**
     * Subscribes to an event that fires when a new host joins the network.
     *
     * @param instanceHostInfoConsumer the {@link InstanceHostInfo}
     *
     * @return the {@link Subscription}
     */
    Subscription subscribeToDiscovery(Consumer<InstanceHostInfo> instanceHostInfoConsumer);

    /**
     * Subscribes to an event that fires when a host leaves the network.
     *
     * @param instanceHostInfoConsumer the {@link InstanceHostInfo}
     * @return the {@link Subscription}
     */
    Subscription subscribeToUndiscovery(Consumer<InstanceHostInfo> instanceHostInfoConsumer);

}
