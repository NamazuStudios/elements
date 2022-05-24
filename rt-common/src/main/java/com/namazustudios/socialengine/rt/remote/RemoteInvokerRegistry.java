package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Manages connections to the cluster.
 */
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
     * Forces or initiates a refresh of all {@link NodeId}s available.  Opening new connections if necessary.  This will
     * block until the refresh operation has been completed and all new connections established (if necessary).
     */
    void refresh();

    /**
     * Gets all {@link RemoteInvoker} instances across all applications.
     *
     * @return the {@link List<RemoteInvoker>}
     */
    List<RemoteInvokerStatus> getAllRemoteInvokerStatuses();

    /**
     * Returns a {@link RemoteInvoker} for all known {@link NodeId}s.  This will be a perfect snapshot of the state
     * of the registry.
     *
     * @return a {@link List<RemoteInvoker>}
     * @param applicationId
     */
    List<RemoteInvokerStatus> getAllRemoteInvokerStatuses(ApplicationId applicationId);

    /**
     * Returns a {@link RemoteInvoker} for all known {@link NodeId}s.  This will be a perfect snapshot of the state
     * of the registry.
     *
     * @return a {@link List<RemoteInvoker>}
     * @param applicationId
     */
    default List<RemoteInvoker> getAllRemoteInvokers(final ApplicationId applicationId) {
        return getAllRemoteInvokerStatuses(applicationId)
            .stream()
            .map(RemoteInvokerStatus::getInvoker)
            .collect(toList());
    }

    /**
     * Returns a {@link RemoteInvoker} by arbitrary selection.  The underlying {@link RemoteInvokerRegistry} may employ
     * heuristics to determine the most suitable {@link RemoteInvoker} to return.  However, this defers entirely to the
     * underlying implementation to make that determination.
     * @param applicationId
     */
    default RemoteInvoker getBestRemoteInvoker(final ApplicationId applicationId) {
        return getBestRemoteInvokerStatus(applicationId).getInvoker();
    }

    /**
     * Returns a {@link RemoteInvoker} by arbitrary selection.  The underlying {@link RemoteInvokerRegistry} may employ
     * heuristics to determine the most suitable {@link RemoteInvoker} to return.  However, this defers entirely to the
     * underlying implementation to make that determination.
     * @param applicationId
     */
    RemoteInvokerStatus getBestRemoteInvokerStatus(final ApplicationId applicationId);

    /**
     * Returns the {@link RemoteInvoker} registered under the given NodeId.
     *
     * @param nodeId the node identifier (may be a NodeId for either an instance or application).
     * @return a RemoteInvoker for the given nodeId, or null if not found.
     */
    RemoteInvoker getRemoteInvoker(NodeId nodeId);

    /**
     * Represents the remote invoker status.
     */
    interface RemoteInvokerStatus {

        /**
         * Gets the {@link NodeId} of the remote connection
         *
         * @return the {@link NodeId}
         */
        NodeId getNodeId();

        /**
         * Gets the priority of the invoker itself.
         *
         * @return the priority
         */
        double getPriority();

        /**
         * Gets the remote invoker.
         *
         * @return the invoker
         */
        RemoteInvoker getInvoker();

    }

}
