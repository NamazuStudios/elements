package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public interface RemoteInvokerRegistry {

    /**
     * Returns a {@link RemoteInvoker}
     */
    RemoteInvoker getAnyRemoteInvoker();

    /**
     * Returns a {@link RemoteInvoker} for all known {@link NodeId}s.
     *
     * @return a {@link List<RemoteInvoker>}
     */
    List<RemoteInvoker> getAllRemoteInvokers();

    /**
     * Returns the {@link RemoteInvoker} registered under the given NodeId.
     *
     * @param nodeId the node identifier (may be a NodeId for either an instance or application).
     * @return a RemoteInvoker for the given nodeId, or null if not found.
     */
    RemoteInvoker getRemoteInvoker(NodeId nodeId);

    /**
     * Gets all {@link RemoteInvoker} instances for the
     *
     * @param nodeIdCollection
     * @return
     */
    default List<RemoteInvoker> getRemoteInvokers(final Collection<NodeId> nodeIdCollection) {
        return nodeIdCollection.stream().map(this::getRemoteInvoker).collect(toList());
    }

}
