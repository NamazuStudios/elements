package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.*;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static java.lang.String.format;

public class JeroMQControlServer {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQControlServer.class);

    private final int index;

    private final ZMQ.Poller poller;

    private final InstanceId instanceId;

    private final JeroMQMultiplexRouter multiplex;

    private final JeroMQDemultiplexRouter demultiplex;

    public JeroMQControlServer(final InstanceId instanceId,
                               final ZMQ.Poller poller, final int index,
                               final JeroMQMultiplexRouter multiplex,
                               final JeroMQDemultiplexRouter demultiplex) {
        this.instanceId = instanceId;
        this.poller = poller;
        this.index = index;
        this.multiplex = multiplex;
        this.demultiplex = demultiplex;
    }

    public void poll() {

        if (!poller.pollin(index)) return;

        final ZMQ.Socket socket = poller.getSocket(index);
        final ZMsg zMsg = ZMsg.recvMsg(socket);

        final JeroMQControlCommand command;

        try {
            command = JeroMQControlCommand.valueOf(zMsg);
        } catch (IllegalArgumentException ex) {
            logger.error("Unknown control command.", ex);
            return;
        }

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
                    response = defaultError(command, zMsg);
                    break;
            }

            response.send(socket);

        } catch (Exception ex) {
            final ZMsg response = exceptionError(command, ex);
            response.send(socket);
        }

    }

    private ZMsg defaultError(final JeroMQControlCommand command, final ZMsg zMsg) {
        logger.error("Unable to handle {} - {}", command, zMsg);
        final ZMsg response = new ZMsg();
        response.addLast(UNKNOWN_COMMAND.toString().getBytes(CHARSET));
        response.addLast(format("Unable to process comand %s", command).getBytes(CHARSET));
        return response;
    }

    private ZMsg exceptionError(final JeroMQControlCommand command, final Exception ex) {

        logger.error("Exception processing request {}", command, ex);
        final ZMsg response = new ZMsg();

        response.addLast(EXCEPTION.toString().getBytes(CHARSET));
        response.addLast(ex.getMessage().getBytes(CHARSET));

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            try (final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(ex);
            }

            response.addLast(bos.toByteArray());

        } catch (IOException e) {
            logger.error("Caught exception serializing exception.", e);
        }

        return response;
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
