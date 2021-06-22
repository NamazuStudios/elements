package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceBinding;

import java.util.concurrent.TimeUnit;

public interface ControlClient extends AutoCloseable {

    /**
     * Gets the routing status for the system.
     *
     * @return the routing status.
     */
    RoutingStatus getRoutingStatus();

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
     * Close the routes via the {@link InstanceId}.  If no routes are known, then nothing happens.
     *
     * @param instanceId the {@link InstanceId}
     *
     */
    void closeRoutesViaInstance(InstanceId instanceId, String instanceConnectAddress);

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
     * Allows for the adjustment of this {@link ControlClient}'s receive timeout.
     *
     * @param timeout the timeout
     * @param timeUnit the {@link TimeUnit}
     */
    void setReceiveTimeout(final long timeout, final TimeUnit timeUnit);

    /**
     * Closes this instance of {@link ControlClient}
     */
    @Override
    void close();

    /**
     * Used to open on-demand instances of the {@link ControlClient}.
     */
    @FunctionalInterface
    interface Factory {

        /**
         * Opens a {@link ControlClient} with the supplied connect address.
         *
         * @param connectAddress the connect address
         * @return the {@link ControlClient}
         */
        ControlClient open(final String connectAddress);

    }

}
