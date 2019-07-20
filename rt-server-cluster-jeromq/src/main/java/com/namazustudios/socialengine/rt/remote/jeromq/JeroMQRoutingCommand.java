package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.NodeId;
import org.zeromq.ZFrame;
import org.zeromq.ZMsg;

public enum JeroMQRoutingCommand {

    /**
     * Requests that the message be routed to a destination.  This is a majority of messages used for invocations.  The
     * mesage will be routed based on subsequent routing information.
     */
    FORWARD,

    /**
     * Gets the instance status.  The instance status returns the instance ID, a listing of nodes IDs and ports
     * currently serving on the instance.
     */
    GET_INSTANCE_STATUS,

    /**
     * Opens a route to a node by specifying the {@link NodeId} and instance connect address.  If a route is already
     * opened then this simply returns the node.
     */
    OPEN_ROUTE_TO_NODE,

    /**
     * Opens a binding for a node by specifying the {@link NodeId}.
     */
    OPEN_BINDING_FOR_NODE,

    /**
     * Closes a binding for a node by specifying the {@link NodeId}.
     */
    CLOSE_BINDING_FOR_NODE;

    private static final JeroMQRoutingCommand VALUES[] = values();

    /**
     * Pushes the command as the first frame in the specified {@link ZMsg}.
     *
     * @param zMsg the {@link ZMsg} to receive the command
     */
    public void pushCommand(final ZMsg zMsg) {

        final byte data[] = new byte[Integer.BYTES];

        int index = 0;
        final int ordinal = ordinal();

        data[index++] = (byte) (ordinal >> (4 * 3));
        data[index++] = (byte) (ordinal >> (4 * 2));
        data[index++] = (byte) (ordinal >> (4 * 1));
        data[index++] = (byte) (ordinal >> (4 * 0));

        final ZFrame frame = new ZFrame(data);
        zMsg.addFirst(frame);

    }

    /**
     * Gets the {@link JeroMQRoutingCommand} or throw an {@link IllegalArgumentException} if the command could not
     * be understood.  This removes the first frame of the message allowing subsequent processing to take place.
     *
     * @param zMsg the message from which to read the command.
     *
     * @return the {@link JeroMQRoutingCommand}
     */
    public static JeroMQRoutingCommand stripCommand(final ZMsg zMsg) {

        final ZFrame frame = zMsg.removeFirst();
        final byte[] data = frame.getData();

        try {

            int i = 0;
            int ordinal = 0;
            ordinal |= (data[i++] << (4 * 3));
            ordinal |= (data[i++] << (4 * 2));
            ordinal |= (data[i++] << (4 * 1));
            ordinal |= (data[i++] << (4 * 0));

            return VALUES[ordinal];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException(ex);
        }

    }

}
