package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.Collection;

import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.pushIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlException.exceptionError;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.OK;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;

public class JeroMQControlServer {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQControlServer.class);

    private final InstanceId instanceId;

    private final JeroMQMultiplexRouter multiplex;

    private final JeroMQDemultiplexRouter demultiplex;

    public JeroMQControlServer(final InstanceId instanceId,
                               final JeroMQMultiplexRouter multiplex,
                               final JeroMQDemultiplexRouter demultiplex) {
        this.instanceId = instanceId;
        this.multiplex = multiplex;
        this.demultiplex = demultiplex;
    }

    /**
     * Handles a {@link ZMsg} coming in from the main socket.  This will handle all control commands and ignore other
     * commands such as {@link JeroMQControlCommand#ROUTE_REQUEST}.
     *
     * @param socket the {@link org.zeromq.ZMQ.Socket}
     * @param zMsg the {@link ZMsg} that was read from the socket
     * @return true if handled, false otherwise
     */
    public boolean handle(final ZMQ.Socket socket,
                          final ZMsg zMsg,
                          final JeroMQControlCommand command,
                          final ZMsg identity) {

        try {

            final ZMsg response;

            switch (command) {
                case GET_INSTANCE_STATUS:
                    response = processInstanceStatus(zMsg);
                    break;
                case OPEN_ROUTE_TO_NODE:
                    response = processOpenRouteToNode(zMsg);
                    break;
                default:
                    return false;
            }

            pushIdentity(response, identity);
            response.send(socket);

            return true;

        } catch (Exception ex) {
            final ZMsg response = exceptionError(ex);
            pushIdentity(response, identity);
            response.send(socket);
            return true;
        }

    }

    private ZMsg processInstanceStatus(final ZMsg zMsg) {
        final ZMsg response = new ZMsg();
        final Collection<NodeId> nodeIds = multiplex.getConnectedPeers();
        if (!zMsg.isEmpty()) logger.warn("Unexpected frames in status request: {}", zMsg);
        response.addLast(OK.toString().getBytes(CHARSET));
        response.addLast(instanceId.asString().getBytes(CHARSET));
        nodeIds.forEach(nid -> response.addLast(nid.asString().getBytes(CHARSET)));
        return response;
    }

    private ZMsg processOpenRouteToNode(final ZMsg zMsg) {
        final ZMsg response = new ZMsg();
        final NodeId nodeId = new NodeId(zMsg.removeFirst().getString(CHARSET));
        final String instanceInvokerAddress = zMsg.removeFirst().getString(CHARSET);
        final String instanceRouteAddress = multiplex.openRouteToNode(nodeId, instanceInvokerAddress);
        response.addLast(OK.toString().getBytes(CHARSET));
        response.addLast(instanceRouteAddress.getBytes(CHARSET));
        return response;
    }

}
