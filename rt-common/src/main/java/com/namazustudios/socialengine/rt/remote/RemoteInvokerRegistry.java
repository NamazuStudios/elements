package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.List;
import java.util.UUID;

public interface RemoteInvokerRegistry {

    /**
     * Starts this {@link RemoteInvokerRegistry}.
     */
    void start();

    /**
     * STops this {@link RemoteInvokerRegistry}.
     */
    void stop();

    /**
     * Returns a {@link RemoteInvoker} by arbitrary selection.  The underlying {@link RemoteInvokerRegistry} may employ
     * heuristics to determine the most suitable {@link RemoteInvoker} to return.  However, this defers entirely to the
     * underlying implementation to make that determination.
     * @param applicationId
     */
    RemoteInvoker getAnyRemoteInvoker(UUID applicationId);

    /**
     * Returns a {@link RemoteInvoker} for all known {@link NodeId}s.  This will be a perfect snapshot of the state
     * of the registry.
     *
     * @return a {@link List<RemoteInvoker>}
     * @param defaultApplicationId
     */
    List<RemoteInvoker> getAllRemoteInvokers(UUID defaultApplicationId);

    /**
     * Returns the {@link RemoteInvoker} registered under the given NodeId.
     *
     * @param nodeId the node identifier (may be a NodeId for either an instance or application).
     * @return a RemoteInvoker for the given nodeId, or null if not found.
     */
    RemoteInvoker getRemoteInvoker(NodeId nodeId);

}
