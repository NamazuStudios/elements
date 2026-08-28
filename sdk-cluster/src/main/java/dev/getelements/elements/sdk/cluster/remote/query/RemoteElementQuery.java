package dev.getelements.elements.sdk.cluster.remote.query;

import dev.getelements.elements.sdk.cluster.address.RemoteElementAddress;
import dev.getelements.elements.sdk.cluster.remote.RemoteElement;
import dev.getelements.elements.sdk.cluster.remote.RemoteElementRegistry;
import dev.getelements.elements.sdk.query.Query;
import dev.getelements.elements.sdk.query.QueryException;

import java.util.Optional;

/**
 * References a {@link RemoteElement} in the {@link RemoteElementRegistry} by its {@link RemoteElementAddress}.
 *
 * @param registry the {@link RemoteElementRegistry} to query
 * @param address the {@link RemoteElementAddress} of the desired {@link RemoteElement}
 */
public record RemoteElementQuery(
        RemoteElementRegistry registry,
        RemoteElementAddress address) implements Query<RemoteElement> {

    @Override
    public Optional<RemoteElement> find() throws QueryException {
        return registry.find(address);
    }

}
