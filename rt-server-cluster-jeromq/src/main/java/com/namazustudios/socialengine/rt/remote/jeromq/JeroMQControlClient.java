package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService;
import org.zeromq.*;

import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingCommand.*;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static org.zeromq.SocketType.DEALER;
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

    private final String instanceConnectAdddress;

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
        this.socket = zContext.createSocket(DEALER);
        this.socket.connect(instanceConnectAddress);
        this.instanceConnectAdddress = instanceConnectAddress;
    }

    /**
     * Gets the {@link InstanceId} for the remote instance.
     *
     * @return the {@link JeroMQInstanceStatus}
     */
    public JeroMQInstanceStatus getInstanceStatus() {

        final ZMsg request = new ZMsg();
        GET_INSTANCE_STATUS.pushCommand(request);
        request.send(socket);

        final ZMsg response = recv();
        return new JeroMQInstanceStatus(response);

    }

    /**
     * Issues the command to open up a route to the node.
     *
     * @param nodeId the {@link NodeId}
     * @param instanceInvokerAddress
     *
     * @return the connect address for the node
     */
    public String openRouteToNode(final NodeId nodeId, final String instanceInvokerAddress) {

        final ZMsg request = new ZMsg();

        OPEN_ROUTE_TO_NODE.pushCommand(request);
        request.add(nodeId.asBytes());
        request.add(instanceInvokerAddress.getBytes(CHARSET));
        request.send(socket);

        final ZMsg response = recv();
        return response.getFirst().getString(CHARSET);

    }

    public InstanceConnectionService.InstanceBinding openBinding(final NodeId nodeId) {

        final ZMsg request = new ZMsg();

        OPEN_BINDING_FOR_NODE.pushCommand(request);
        request.add(nodeId.asBytes());
        request.send(socket);

        final ZMsg response = recv();
        final String nodeBindAddress = response.removeFirst().getString(CHARSET);
        return new JeroMQInstanceBinding(zContext, nodeId, instanceConnectAdddress, nodeBindAddress);

    }

    public void closeBinding(final NodeId nodeId) {

        final ZMsg request = new ZMsg();

        CLOSE_BINDING_FOR_NODE.pushCommand(request);
        request.add(nodeId.asBytes());
        request.send(socket);

        recv();

    }

    private ZMsg recv() {
        final ZMsg response = ZMsg.recvMsg(socket);
        return check(response);
    }

    private ZMsg check(final ZMsg response) {

        JeroMQControlResponseCode code;

        try {
            code = JeroMQControlResponseCode.stripCode(response);
        } catch (IllegalArgumentException ex) {
            code = JeroMQControlResponseCode.UNKNOWN_ERROR;
        }

        switch (code) {
            case OK: return response;
            default: throw new JeroMQControlException(code, response);
        }

    }

    @Override
    public void close() {
        socket.close();
        zContext.close();
    }
}
