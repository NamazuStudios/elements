package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceBinding;

public interface ControlClient extends AutoCloseable {

    /**
     * Gets the {@link InstanceId} for the remote instance.
     *
     * @return the {@link InstanceStatus}
     */
    InstanceStatus getInstanceStatus();

    /**
     * Issues the command to open up a route to the node.
     *
     * @param nodeId the {@link NodeId}
     * @param instanceInvokerAddress
     *
     * @return the connect address for the node
     */
    String openRouteToNode(NodeId nodeId, String instanceInvokerAddress);

    /**
     * Close the route to the {@link NodeId}.  If the route is not known, then nothing happens.
     *
     * @param nodeId the {@link NodeId}
     *
     */
    void closeRouteToNode(NodeId nodeId);

    /**
     * Close the routes via the {@link InstanceId}.  If no routes are known, then nothing happens.
     *
     * @param instanceId the {@link InstanceId}
     *
     */
    void closeRoutesViaInstance(InstanceId instanceId);

    /**
     * Opens an {@link InstanceBinding} provided the {@link NodeId} and returns the {@link InstanceBinding}.
     *
     * @param nodeId the {@link NodeId}
     * @return the {@link InstanceBinding}
     */
    InstanceBinding openBinding(NodeId nodeId);

    /**
     * Issues the command to close a binding.  This will invalidate all {@link InstanceBinding}s to that {@link NodeId}.
     *
     * @param nodeId
     */
    void closeBinding(NodeId nodeId);

    /**
     * Closes this instance of {@link ControlClient}
     */
    @Override
    void close();

}
