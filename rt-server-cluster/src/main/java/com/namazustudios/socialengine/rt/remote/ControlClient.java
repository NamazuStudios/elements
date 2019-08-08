package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;

public interface ControlClient extends AutoCloseable {
    /**
     * Gets the {@link InstanceId} for the remote instance.
     *
     * @return the {@link JeroMQInstanceStatus}
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

    InstanceConnectionService.InstanceBinding openBinding(NodeId nodeId);

    void closeBinding(NodeId nodeId);

    @Override
    void close();
}
