package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.NodeId;
import org.zeromq.ZFrame;
import org.zeromq.ZMsg;

import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;

public enum JeroMQControlCommand {

    /**
     * Gets the instance status.  The instance status returns the instance ID, a listing of nodes IDs and ports
     * currently serving on the instance.
     */
    GET_INSTANCE_STATUS,

    /**
     * Opens a route to a node by specifying the {@link NodeId} and connect address.  If a route is already opened
     * then this simply returns the node.
     */
    OPEN_ROUTE_TO_NODE;

    /**
     * Gets the {@link JeroMQControlCommand} or throw an {@link IllegalArgumentException} if the command could not
     * be understood.  This removes the first frame of the message allowing subsequent processing to take place.
     *
     * @param zMsg the message from which to read the command.
     *
     * @return the {@link JeroMQControlCommand}
     */
    public static JeroMQControlCommand valueOf(final ZMsg zMsg) {
        final ZFrame frame = zMsg.removeFirst();
        return valueOf(frame.getString(CHARSET));
    }

}
