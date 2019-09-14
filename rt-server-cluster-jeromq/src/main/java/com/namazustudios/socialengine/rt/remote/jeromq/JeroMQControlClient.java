package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.ControlClient;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService;
import com.namazustudios.socialengine.rt.remote.InstanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import java.util.concurrent.TimeUnit;

import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.EMPTY_DELIMITER;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.PROTOCOL_ERROR;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.SOCKET_ERROR;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingCommand.*;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.zeromq.SocketType.DEALER;
import static org.zeromq.ZContext.shadow;

/**
 * Implements the control protocol for interfacing with the {@link JeroMQInstanceConnectionService}.  This client
 * is responsible for performing control operations such as registering a node, deregistering a node, and querying
 * available nodes.
 *
 * This class is designed to be used by a single thread and destroyed.
 */
public class JeroMQControlClient implements ControlClient {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQControlClient.class);

    public static final long DEFAULT_TIMEOUT = 30;

    public static final TimeUnit DEFAULT_TIMEOUT_UNITS = SECONDS;

    private final ZContext shadowContext;

    private final ZMQ.Socket socket;

    private final String instanceConnectAdddress;

    public JeroMQControlClient(final ZContext zContext,
                               final String instanceConnectAddress) {
        this(zContext, instanceConnectAddress, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNITS);
    }

    /**
     * Creates a {@link JeroMQControlClient} connecting to the remote instance to perform basic discover and control
     * operations.  This uses {@link ZContext#shadow(ZContext)} to make a shadow copy of the {@link ZContext} and then
     * closes it later.
     *
     * @param zContext the {@link ZContext} used to communicate
     * @param instanceConnectAddress
     */
    public JeroMQControlClient(final ZContext zContext,
                               final String instanceConnectAddress,
                               final long timeout, final TimeUnit timeUnit) {
        this.shadowContext = shadow(zContext);
        this.socket = shadowContext.createSocket(DEALER);
        this.socket.connect(instanceConnectAddress);
        this.instanceConnectAdddress = instanceConnectAddress;
        this.socket.setReceiveTimeOut((int) MILLISECONDS.convert(timeout, timeUnit));
    }

    @Override
    public InstanceStatus getInstanceStatus() {

        final ZMsg request = new ZMsg();
        GET_INSTANCE_STATUS.pushCommand(request);
        send(request);

        final ZMsg response = recv();
        return new JeroMQInstanceStatus(response);

    }

    @Override
    public String openRouteToNode(final NodeId nodeId, final String instanceInvokerAddress) {

        logger.debug("Opening route to node {} at {}", nodeId, instanceInvokerAddress);

        final ZMsg request = new ZMsg();

        OPEN_ROUTE_TO_NODE.pushCommand(request);
        request.add(nodeId.asBytes());
        request.add(instanceInvokerAddress.getBytes(CHARSET));
        send(request);

        final ZMsg response = recv();
        return response.getFirst().getString(CHARSET);

    }

    @Override
    public void closeRouteToNode(final NodeId nodeId) {

        logger.debug("Closing route to node {}", nodeId);

        final ZMsg request = new ZMsg();
        CLOSE_ROUTE_TO_NODE.pushCommand(request);
        request.add(nodeId.asBytes());
        send(request);
        recv();
    }

    @Override
    public void closeRoutesViaInstance(final InstanceId instanceId) {

        logger.debug("Closing all routes for instance {}", instanceId);

        final ZMsg request = new ZMsg();
        CLOSE_ROUTES_VIA_INSTANCE.pushCommand(request);
        request.add(instanceId.asBytes());
        send(request);
        recv();
    }

    @Override
    public InstanceConnectionService.InstanceBinding openBinding(final NodeId nodeId) {

        logger.debug("Opening binding for {}", nodeId);

        final ZMsg request = new ZMsg();

        OPEN_BINDING_FOR_NODE.pushCommand(request);
        request.add(nodeId.asBytes());
        send(request);

        final ZMsg response = recv();
        final String nodeBindAddress = response.removeFirst().getString(CHARSET);
        return new JeroMQInstanceBinding(shadowContext, nodeId, instanceConnectAdddress, nodeBindAddress);

    }

    @Override
    public void closeBinding(final NodeId nodeId) {

        logger.debug("Closing binding for {}", nodeId);

        final ZMsg request = new ZMsg();
        CLOSE_BINDING_FOR_NODE.pushCommand(request);
        request.add(nodeId.asBytes());
        send(request);
        recv();

    }

    private void send(final ZMsg zMsg) {
        zMsg.addFirst(EMPTY_DELIMITER);
        zMsg.send(socket);
    }

    private ZMsg recv() {
        final ZMsg response = ZMsg.recvMsg(socket);
        return check(response);
    }

    private ZMsg check(final ZMsg response) {

        if (response == null) throw new JeroMQControlException(SOCKET_ERROR);
        if (response.isEmpty()) throw new JeroMQControlException(PROTOCOL_ERROR);

        final ZFrame delimter = response.removeFirst();
        if (delimter.getData().length != 0) throw new JeroMQControlException(PROTOCOL_ERROR);

        JeroMQControlResponseCode code;

        try {
            code = JeroMQControlResponseCode.stripCode(response);
        } catch (IllegalArgumentException ex) {
            code = JeroMQControlResponseCode.UNKNOWN_ERROR;
        }

        switch (code) {
            case OK: return response;
            case NO_SUCH_NODE_ROUTE: throw new JeroMQUnroutableNodeException(response);
            default: throw new JeroMQControlException(code, response);
        }

    }

    @Override
    public void close() {
        socket.close();
        shadowContext.close();
    }
}
