package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.id.NodeId;

public interface RemoteInvokerRegistry {

    /**
     * Returns a {@link RemoteInvoker}
     */
    RemoteInvoker getAnyRemoteInvoker();

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
