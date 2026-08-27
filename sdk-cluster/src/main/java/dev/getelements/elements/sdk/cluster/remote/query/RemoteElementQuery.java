package dev.getelements.elements.sdk.cluster.remote.query;

import dev.getelements.elements.sdk.cluster.remote.RemoteElement;
import dev.getelements.elements.sdk.cluster.remote.RemoteElementRegistry;
import dev.getelements.elements.sdk.query.Query;

/**
 * References a {@link RemoteElement} in the {@}
 * @param registry
 */
public record RemoteElementQuery(RemoteElementRegistry registry) implements Query<RemoteElement> {
}
