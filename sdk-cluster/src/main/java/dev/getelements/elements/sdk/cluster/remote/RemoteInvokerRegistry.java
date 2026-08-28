package dev.getelements.elements.sdk.cluster.remote;

import dev.getelements.elements.sdk.cluster.address.RemoteInstanceSelector;
import dev.getelements.elements.sdk.cluster.remote.routing.RoutingStrategy;

import java.util.List;

/**
 * Tracks the set of live {@link RemoteInvoker} instances in the cluster and allows callers to select one or more of
 * them, according to a {@link RemoteInstanceSelector}, in order to dispatch an invocation.
 */
public interface RemoteInvokerRegistry {

    /**
     * Gets all {@link RemoteInvoker} instances matching the supplied {@link RemoteInstanceSelector}.
     *
     * @param remoteInstanceSelector the {@link RemoteInstanceSelector} used to select the {@link RemoteInvoker}s
     * @return the list of matching {@link RemoteInvoker} instances, never null
     */
    List<RemoteInvoker> getAllRemoteInvokers(RemoteInstanceSelector remoteInstanceSelector);

    /**
     * Gets the best available {@link RemoteInvoker} matching the supplied {@link RemoteInstanceSelector}.
     *
     * @param remoteInstanceSelector the {@link RemoteInstanceSelector} used to select the {@link RemoteInvoker}
     * @return the best matching {@link RemoteInvoker}
     */
    RemoteInvoker getBestRemoteInvoker(RemoteInstanceSelector remoteInstanceSelector);

}
