package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.NodeId;

public enum JeroMQControlCommand {

    /**
     * Gets the instance status.  The instance status returns the instance ID, a listing of nodes IDs and ports
     * currently serving on the instance.
     */
    GET_INSTANCE_STATUS,

    /**
     * Opens a route to a node by specifying the {@link NodeId} and connect address.
     */
    OPEN_ROUTE_TO_NODE

}
