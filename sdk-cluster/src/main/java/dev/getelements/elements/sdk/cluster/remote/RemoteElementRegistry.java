package dev.getelements.elements.sdk.cluster.remote;

import dev.getelements.elements.sdk.cluster.address.RemoteElementAddress;

import java.util.Optional;

/**
 * Tracks the {@link RemoteElement} instances hosted by this node, keyed by their {@link RemoteElementAddress}.
 */
public interface RemoteElementRegistry {

    /**
     * Looks up the {@link RemoteElement} registered at the supplied {@link RemoteElementAddress}.
     *
     * @param address the {@link RemoteElementAddress}
     * @return an {@link Optional} containing the {@link RemoteElement}, or empty if none is registered
     */
    Optional<RemoteElement> find(RemoteElementAddress address);

}
