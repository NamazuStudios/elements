package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.NodeId;
import com.namazustudios.socialengine.rt.exception.NodeNotFoundException;

import java.util.Set;
import java.util.UUID;

public interface RemoteInvokerRegistry {
    /**
     * Returns a randomly-selected instance-level {@link RemoteInvoker}, or null if none are currently registered.
     * @return {@link RemoteInvoker} or null.
     */
    RemoteInvoker getAnyInstanceRemoteInvoker();

    /**
     * Returns a randomly-selected application-level {@link RemoteInvoker}, or null if none are currently registered.
     * @return {@link RemoteInvoker} or null.
     */
    RemoteInvoker getAnyApplicationRemoteInvoker();

    /**
     * Returns all registered Instance-level {@link RemoteInvoker}s.
     * @return a {@link Set} of {@link RemoteInvoker}s.
     */
    Set<RemoteInvoker> getAllInstanceRemoteInvokers();

    /**
     * Returns all registered Application-level {@link RemoteInvoker}s.
     * @return a {@link Set} of {@link RemoteInvoker}s.
     */
    Set<RemoteInvoker> getAllApplicationRemoteInvokers();

    /**
     * Returns the {@link RemoteInvoker} registered under the given NodeId. It may be a {@link NodeId} for
     * either an instance or an application. If the category of Node is known beforehand (i.e. instance or application),
     * the direct accessors {@link RemoteInvokerRegistry#getApplicationRemoteInvoker(NodeId)} and
     * {@link RemoteInvokerRegistry#getInstanceRemoteInvoker(NodeId)} should be utilized instead.
     *
     * @param nodeId the node identifier (may be a NodeId for either an instance or application).
     * @return a RemoteInvoker for the given nodeId, or null if not found.
     */
    RemoteInvoker getRemoteInvoker(NodeId nodeId);

    /**
     * Returns the instance-level {@link RemoteInvoker} registered under the given NodeId, or null if not found.
     *
     * @param nodeId
     * @return a RemoteInvoker for the given nodeId, or null if not found.
     */
    RemoteInvoker getInstanceRemoteInvoker(NodeId nodeId);

    /**
     * Returns the application-level {@link RemoteInvoker} registered under the given NodeId, or null if not found.
     *
     * @param nodeId
     * @return a RemoteInvoker for the given nodeId, or null if not found.
     */
    RemoteInvoker getApplicationRemoteInvoker(NodeId nodeId);
}
