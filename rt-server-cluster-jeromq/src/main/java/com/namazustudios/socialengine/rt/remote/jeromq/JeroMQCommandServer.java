package com.namazustudios.socialengine.rt.remote.jeromq;

import com.google.common.collect.SortedSetMultimap;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.slf4j.Logger;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import zmq.Command;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.id.NodeId.nodeIdFromBytes;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.popIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.IdentityUtil.pushIdentity;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.OK;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.UNKNOWN_COMMAND;
import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.*;

public class JeroMQCommandServer {

    public static final String TRACE_DELIMITER = "<Client Stack Trace>";

    private static final ZFrame TRACE_DELIMITER_FRAME = new ZFrame(TRACE_DELIMITER.getBytes(CHARSET));

    private final Logger logger;

    private final InstanceId instanceId;

    private final ZMQ.Poller poller;

    private final int frontend;

    private final JeroMQMultiplexRouter multiplex;

    private final JeroMQDemultiplexRouter demultiplex;

    private final Stats stats;

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
        this.stats = new Stats();
    }

    public void poll() {
        if (!poller.pollin(frontend)) return;
        final var socket = poller.getSocket(frontend);
        final var zMsg = ZMsg.recvMsg(socket);
        handle(socket, zMsg);
    }

    private void handle(final ZMQ.Socket socket, final ZMsg zMsg) {

        final JeroMQRoutingCommand command;
        final var identity = popIdentity(zMsg);

        try {
            command = JeroMQRoutingCommand.stripCommand(zMsg);
            stats.increment(command);
        } catch (Exception ex) {
            final ZMsg response = exceptionError(logger, ex);
            pushIdentity(response, identity);
            response.send(socket);
            stats.error();
            logTrace(null, zMsg);
            return;
        }

        try {

            final ZMsg response;

            switch (command) {
                case FORWARD:
                    demultiplex.forward(zMsg, identity);
                    return;
                case GET_ROUTING_STATUS:
                    response = processRoutingStatus(zMsg);
                    break;
                case GET_INSTANCE_STATUS:
                    response = processInstanceStatus(zMsg);
                    break;
                case OPEN_ROUTE_TO_NODE:
                    response = processOpenRouteToNode(zMsg);
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
            final var response = exceptionError(logger, ex.getCode(), ex);
            response.addLast(ex.getNodeId().asBytes());
            pushIdentity(response, identity);
            response.send(socket);
        } catch (JeroMQUnroutableInstanceException ex) {
            final var response = exceptionError(logger, ex.getCode(), ex);
            response.addLast(ex.getInstanceId().asBytes());
            pushIdentity(response, identity);
            response.send(socket);
        } catch (JeroMQControlException ex) {
            final var response = exceptionError(logger, ex.getCode(), ex);
            pushIdentity(response, identity);
            response.send(socket);
        } catch (Exception ex) {
            final var response = exceptionError(logger, ex);
            pushIdentity(response, identity);
            response.send(socket);
        }

        logTrace(command, zMsg);

    }

    private void logTrace(final JeroMQRoutingCommand command, final ZMsg zMsg) {

        if (!logger.isDebugEnabled()) return;

        final var iterator = zMsg.iterator();
        while (iterator.hasNext() && !iterator.next().hasSameData(TRACE_DELIMITER_FRAME));

        final var sb = new StringBuffer();
        sb.append("\nCommand '").append(command).append("' stack Trace:");

        while (iterator.hasNext()) {
            final var element = iterator.next().getString(CHARSET);
            sb.append("\n    ").append(element);
        }

        logger.debug("{}", sb);

    }

    private ZMsg processOpenBindingForNode(final ZMsg zMsg) {
        final var response = new ZMsg();
        final var nodeId = nodeIdFromBytes(zMsg.removeFirst().getData());
        final var instanceBindAddress = demultiplex.openBinding(nodeId);
        logger.info("Opened binding for node {} via {}", nodeId, instanceBindAddress);
        OK.pushResponseCode(response);
        response.addLast(instanceBindAddress.getBytes(CHARSET));
        return response;
    }

    private ZMsg processCloseBindingForNode(final ZMsg zMsg) {
        final var response = new ZMsg();
        final var nodeId = nodeIdFromBytes(zMsg.removeFirst().getData());
        demultiplex.closeBindingForNode(nodeId);
        logger.info("Closed binding for node {}", nodeId);
        OK.pushResponseCode(response);
        return response;
    }

    private ZMsg processRoutingStatus(final ZMsg zMsg) {

        final var response = new ZMsg();

        logger.debug("Got routing table.");
        OK.pushResponseCode(response);

        response.addLast(instanceId.asBytes());
        multiplex.getRoutingTable().forEach((nid, conn) -> {
            response.addLast(nid.asBytes());
            response.addLast(conn.toString());
        });

        return response;

    }

    private ZMsg processInstanceStatus(final ZMsg zMsg) {
        final ZMsg response = new ZMsg();
        final Collection<NodeId> nodeIds = demultiplex.getConnectedNodeIds();
        logger.debug("Got instance status.");
        OK.pushResponseCode(response);
        response.addLast(instanceId.asBytes());
        nodeIds.forEach(nid -> response.addLast(nid.asBytes()));
        return response;
    }

    private ZMsg processOpenRouteToNode(final ZMsg zMsg) {
        final var response = new ZMsg();
        final var nodeId = nodeIdFromBytes(zMsg.removeFirst().getData());
        final var instanceInvokerAddress = zMsg.removeFirst().getString(CHARSET);
        final var instanceRouteAddress = multiplex.openRouteToNode(nodeId, instanceInvokerAddress);
        logger.info("Opened route to {} via {} -> {}", nodeId, instanceRouteAddress, instanceInvokerAddress);
        OK.pushResponseCode(response);
        response.addLast(instanceRouteAddress.getBytes(CHARSET));
        return response;
    }

    private ZMsg processCloseRoutesViaInstance(final ZMsg zMsg) {
        final var response = new ZMsg();
        final var instanceId = new InstanceId(zMsg.removeFirst().getData());
        final var instanceConnectAddress = zMsg.removeFirst().getString(CHARSET);
        multiplex.closeRoutesViaInstance(instanceId, instanceConnectAddress);
        logger.info("Closed routes via instance {}.", instanceId);
        OK.pushResponseCode(response);
        return response;
    }

    public void log() {
        stats.log();
    }

    private class Stats {

        private final JeroMQDebugCounter errorCounter = new JeroMQDebugCounter();

        private final Map<JeroMQRoutingCommand, JeroMQDebugCounter> counters = new EnumMap<>(JeroMQRoutingCommand.class);

        private final Runnable error = logger.isDebugEnabled() ? errorCounter::increment : () -> {};

        private final Consumer<JeroMQRoutingCommand> increment = logger.isDebugEnabled()
            ? command -> counters.computeIfAbsent(command, c -> new JeroMQDebugCounter()).increment()
            : command -> {};

        private final Runnable log = logger.isDebugEnabled() ? this::doLog : () -> {};

        private void doLog() {
            logger.debug("Command Server Stats:");
            logger.debug("  Errors {}", errorCounter);
            counters.forEach((k, v) -> logger.debug("  Processed {} {}", k, v));
        }

        public void log() {
            log.run();
        }

        public void error() {
            error.run();
        }

        public void increment(final JeroMQRoutingCommand command) {
            this.increment.accept(command);
        }

    }

}
