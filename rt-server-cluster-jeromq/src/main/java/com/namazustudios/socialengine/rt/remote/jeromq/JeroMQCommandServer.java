package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.Collection;

import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.popIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.pushIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.OK;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.*;

public class JeroMQCommandServer {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQCommandServer.class);

    private final InstanceId instanceId;

    private final ZMQ.Poller poller;

    private final int frontend;

    private final JeroMQMultiplexRouter multiplex;

    private final JeroMQDemultiplexRouter demultiplex;

    public JeroMQCommandServer(final InstanceId instanceId,
                               final ZMQ.Poller poller, final int frontend,
                               final JeroMQMultiplexRouter multiplex,
                               final JeroMQDemultiplexRouter demultiplex) {
        this.instanceId = instanceId;
        this.poller = poller;
        this.frontend = frontend;
        this.multiplex = multiplex;
        this.demultiplex = demultiplex;
    }

    public void poll() {
        if (!poller.pollin(frontend)) return;
        final ZMQ.Socket socket = poller.getSocket(frontend);
        final ZMsg zMsg = ZMsg.recvMsg(socket);
        handle(socket, zMsg);
    }

    private void handle(final ZMQ.Socket socket, final ZMsg zMsg) {

        final JeroMQRoutingCommand command;
        final ZMsg identity = popIdentity(zMsg);

        try {
            command = JeroMQRoutingCommand.stripCommand(zMsg);
        } catch (IllegalArgumentException ex) {
            final ZMsg response = exceptionError(ex);
            pushIdentity(response, identity);
            response.send(socket);
            return;
        }

        try {

            final ZMsg response;

            switch (command) {
                case FORWARD:
                    demultiplex.forward(zMsg, identity);
                    return;
                case GET_INSTANCE_STATUS:
                    response = processInstanceStatus(zMsg);
                    break;
                case OPEN_ROUTE_TO_NODE:
                    response = processOpenRouteToNode(zMsg);
                    break;
                default:
                    response = error("Unsupported message type: " + command);
                    break;
            }

            pushIdentity(response, identity);
            response.send(socket);

        } catch (Exception ex) {
            final ZMsg response = exceptionError(ex);
            pushIdentity(response, identity);
            response.send(socket);
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
