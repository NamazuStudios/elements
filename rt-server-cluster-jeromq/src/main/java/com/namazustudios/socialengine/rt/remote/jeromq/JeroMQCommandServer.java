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
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.UNKNOWN_COMMAND;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.*;
import static java.lang.String.format;

public class JeroMQCommandServer {

    private final Logger logger;

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
        this.logger = getLogger(getClass(), instanceId);
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
        } catch (Exception ex) {
            final ZMsg response = exceptionError(logger, ex);
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
                case CLOSE_ROUTE_TO_NODE:
                    response = processCloseRouteToNode(zMsg);
                    break;
                case OPEN_BINDING_FOR_NODE:
                    response = processOpenBindingForNode(zMsg);
                    break;
                case CLOSE_BINDING_FOR_NODE:
                    response = processCloseBindingForNode(zMsg);
                    break;
                case CLOSE_ROUTES_VIA_INSTANCE:
                    response = processCloseRoutesViaInstance(zMsg);
                    break;
                default:
                    response = error(UNKNOWN_COMMAND, "Unsupported message type: " + command);
                    break;
            }

            pushIdentity(response, identity);
            response.send(socket);

        } catch (JeroMQUnroutableNodeException ex) {
            final ZMsg response = exceptionError(logger, ex.getCode(), ex);
            response.addLast(ex.getNodeId().asBytes());
            pushIdentity(response, identity);
            response.send(socket);
        } catch (JeroMQUnroutableInstanceException ex) {
            final ZMsg response = exceptionError(logger, ex.getCode(), ex);
            response.addLast(ex.getInstanceId().asBytes());
            pushIdentity(response, identity);
            response.send(socket);
        } catch (JeroMQControlException ex) {
            final ZMsg response = exceptionError(logger, ex.getCode(), ex);
            pushIdentity(response, identity);
            response.send(socket);
        } catch (Exception ex) {
            final ZMsg response = exceptionError(logger, ex);
            pushIdentity(response, identity);
            response.send(socket);
        }

    }

    private ZMsg processOpenBindingForNode(final ZMsg zMsg) {
        final ZMsg response = new ZMsg();
        final NodeId nodeId = new NodeId(zMsg.removeFirst().getData());
        final String instanceBindAddress = demultiplex.openBinding(nodeId);
        logger.info("Opened binding for node {} via {}", nodeId, instanceBindAddress);
        OK.pushResponseCode(response);
        response.addLast(instanceBindAddress.getBytes(CHARSET));
        return response;
    }

    private ZMsg processCloseBindingForNode(final ZMsg zMsg) {
        final ZMsg response = new ZMsg();
        final NodeId nodeId = new NodeId(zMsg.removeFirst().getData());
        demultiplex.closeBindingForNode(nodeId);
        logger.info("Closed binding for node {}");
        OK.pushResponseCode(response);
        return response;
    }

    private ZMsg processInstanceStatus(final ZMsg zMsg) {
        final ZMsg response = new ZMsg();
        final Collection<NodeId> nodeIds = demultiplex.getConnectedNodeIds();
        if (!zMsg.isEmpty()) logger.warn("Unexpected frames in status request: {}", zMsg);
        logger.debug("Got instance status.");
        OK.pushResponseCode(response);
        response.addLast(instanceId.asBytes());
        nodeIds.forEach(nid -> response.addLast(nid.asBytes()));
        return response;
    }

    private ZMsg processOpenRouteToNode(final ZMsg zMsg) {
        final ZMsg response = new ZMsg();
        final NodeId nodeId = new NodeId(zMsg.removeFirst().getData());
        final String instanceInvokerAddress = zMsg.removeFirst().getString(CHARSET);
        final String instanceRouteAddress = multiplex.openRouteToNode(nodeId, instanceInvokerAddress);
        logger.info("Opened route to {} via {} -> {}", nodeId, instanceRouteAddress, instanceInvokerAddress);
        OK.pushResponseCode(response);
        response.addLast(instanceRouteAddress.getBytes(CHARSET));
        return response;
    }

    private ZMsg processCloseRouteToNode(final ZMsg zMsg) {
        final ZMsg response = new ZMsg();
        final NodeId nodeId = new NodeId(zMsg.removeFirst().getData());
        multiplex.closeRouteToNode(nodeId);
        logger.info("Closed route to {}.", nodeId);
        OK.pushResponseCode(response);
        return response;
    }

    private ZMsg processCloseRoutesViaInstance(final ZMsg zMsg) {
        final ZMsg response = new ZMsg();
        final InstanceId instanceId = new InstanceId(zMsg.removeFirst().getData());
        multiplex.closeRoutesViaInstance(instanceId);
        logger.info("Closed routes via instance {}.", instanceId);
        OK.pushResponseCode(response);
        return response;
    }

}
