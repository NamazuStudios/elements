package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import static org.zeromq.ZContext.shadow;

/**
 * Implements the control protocol for interfacing with the {@link JeroMQInstanceConnectionService}.  This client
 * is responsible for performing control operations such as registering a node, deregistering a node, and querying
 * available nodes.
 *
 * This class is designed to be used by a single thread and destroyed.
 */
public class JeroMQControlClient implements AutoCloseable {

    private final ZContext zContext;

    private final ZMQ.Socket socket;

    /**
     * Creates a {@link JeroMQControlClient} connecting to the remote instance to perform basic discover and control
     * operations.  This uses {@link ZContext#shadow(ZContext)} to make a shadow copy of the {@link ZContext} and then
     * closes it later.
     *
     * @param zContext the {@link ZContext} used to communicate
     * @param instanceConnectAddress
     */
    public JeroMQControlClient(final ZContext zContext, final String instanceConnectAddress) {
        this.zContext = shadow(zContext);
        this.socket = zContext.createSocket(SocketType.REQ);
        this.socket.connect(instanceConnectAddress);
    }

    /**
     * Gets the {@link InstanceId} for the remote instance.
     *
     * @return the {@link JeroMQInstanceStatus}
     */
    public JeroMQInstanceStatus getInstanceStatus() {
        // TODO Implement this
        return null;
    }

    /**
     * Issues the command to open up a route to the node.
     *
     * @param nodeId the {@link NodeId}
     *
     * @return the connect address for the node
     */
    public String openRouteToNode(final NodeId nodeId) {
        // TODO Implement this
        return null;
    }

    /**
     * Gets the connect address for the supplied {@link NodeId}
     *
     * @param nodeId the {@link NodeId}
     *
     * @return the connect address.
     */
    public String getConnectAddress(final NodeId nodeId) {
        return null;
    }

    @Override
    public void close() {
        socket.close();
        zContext.close();
    }

}
