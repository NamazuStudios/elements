package com.namazustudios.socialengine.rt.jeromq;


import com.namazustudios.socialengine.rt.NodeId;
import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMsg;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandPreambleFromBytes;
import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType.*;
import static com.namazustudios.socialengine.rt.remote.RoutingCommand.RoutingCommandFromBytes;
import static com.namazustudios.socialengine.rt.remote.SocketHandleRegistry.SOCKET_HANDLE_NOT_FOUND;
import static com.namazustudios.socialengine.rt.remote.StatusRequest.buildStatusRequest;
import static com.namazustudios.socialengine.rt.remote.StatusResponse.buildStatusResponse;
import static com.namazustudios.socialengine.rt.remote.StatusResponse.StatusResponseFromBytes;
import static com.namazustudios.socialengine.rt.remote.InstanceConnectionCommand.InstanceConnectionCommandFromBytes;
import static com.namazustudios.socialengine.rt.remote.InstanceUuidListResponse.buildInstanceUuidListResponse;
import com.namazustudios.socialengine.rt.remote.RoutingCommand.Action;
import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType;
import static com.namazustudios.socialengine.rt.jeromq.ControlMessageBuilder.buildControlMsg;
import com.namazustudios.socialengine.rt.remote.RoutingHeader.Status;
import static com.namazustudios.socialengine.rt.remote.RoutingHeader.Status.*;
import static org.zeromq.ZMQ.*;

/**
 * Handles ZMQ messages. This is not thread-safe and is meant to be self-contained, i.e. method calls within
 * this module should only originate from received ZMQ messages that this module consumes (or from high-level close
 * commands from AutoClosable/thread shutdown).
 */
public class MessageManager implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MessageManager.class);

    private final MessageManagerConfiguration messageManagerConfiguration;

    private final SocketHandleRegistry socketHandleRegistry = new SocketHandleRegistry();

    //================================================================================
    //
    // Constructor
    //
    //================================================================================

    public MessageManager(final MessageManagerConfiguration messageManagerConfiguration) {
        this.messageManagerConfiguration = messageManagerConfiguration;
    }

    private void sendStatusRequest(final int socketHandle, final ConnectionsManager connectionsManager) {
        final StatusRequest statusRequest = buildStatusRequest();
        final ZMsg requestMsg = buildControlMsg(STATUS_REQUEST, statusRequest.getByteBuffer());

        connectionsManager.sendMsgToSocketHandle(socketHandle, requestMsg);
    }

    private void sendStatusResponse(final int socketHandle, final ConnectionsManager connectionsManager) {
        // build the response with status data
        final StatusResponse statusResponse = buildStatusResponse(messageManagerConfiguration.getInstanceUuid());
        final ZMsg responseMsg = buildControlMsg(STATUS_RESPONSE, statusResponse.getByteBuffer());

        // and send the response back over the same pipe
        connectionsManager.sendMsgToSocketHandle(socketHandle, responseMsg);
    }

    private void sendInstanceUuidListResponse(final int socketHandle, final ConnectionsManager connectionsManager) {
        final List<ByteBuffer> instanceUuidListResponseByteBuffers = socketHandleRegistry
            .getRegisteredInstanceUuids()
            .stream()
            .map(instanceUuid -> buildInstanceUuidListResponse(instanceUuid).getByteBuffer())
            .collect(Collectors.toList());

        // TODO: build out instance availability service, it will have a control conn that will poll INSTANCE_UUID_LIST_REQUEST,
        //  pop the preamble, then iterate over the remaining zframes to build the current instance uuid list
        final ZMsg responseMsg = buildControlMsg(INSTANCE_UUID_LIST_RESPONSE, instanceUuidListResponseByteBuffers);

        connectionsManager.sendMsgToSocketHandle(socketHandle, responseMsg);
    }

    private void sendRoutingCommandAcknowledgement(
            final int socketHandle,
            final ConnectionsManager connectionsManager) {
        // build the ack msg
        final RoutingCommandAcknowledgement routingCommandAcknowledgement = new RoutingCommandAcknowledgement();
        final ZMsg acknowledgementMsg =
                buildControlMsg(ROUTING_COMMAND_ACK, routingCommandAcknowledgement.getByteBuffer());

        // and send the msg back over the same pipe
        connectionsManager.sendMsgToSocketHandle(socketHandle, acknowledgementMsg);
    }

    private void handleInstanceConnectionCommand(
            final InstanceConnectionCommand instanceConnectionCommand,
            final ConnectionsManager connectionsManager
    ) {
        final InstanceConnectionCommand.Action action = instanceConnectionCommand.action.get();
        final String invokerTcpAddress = instanceConnectionCommand.invokerTcpAddress.get();
        final String controlTcpAddress = instanceConnectionCommand.controlTcpAddress.get();

        // TODO: delete this log
        logger.info("Ctrl-bind: recv inst conn cmd: {}, invk: {}, ctrl: {}", action, invokerTcpAddress, controlTcpAddress);

        switch (action) {
            case NO_OP: {
                break;
            }
            case CONNECT: {
                handleConnectInstanceCommand(invokerTcpAddress, controlTcpAddress, connectionsManager);
                break;
            }
            case DISCONNECT: {
                handleDisconnectInstanceCommand(invokerTcpAddress, controlTcpAddress, connectionsManager);
            }
            default: {
                logger.warn("Ctrl-bind: Encountered unhandled instance connection action: {}. Dropping message.", action);
                break;
            }
        }
    }

    private void handleStatusResponse(
        final int controlSocketHandle,
        StatusResponse statusResponse
    ) {
        final UUID instanceUuid = statusResponse.instanceUuid.get();
        socketHandleRegistry.registerInstanceUuid(controlSocketHandle, instanceUuid);
    }

    private void handleConnectInstanceCommand(
        final String invokerTcpAddress,
        final String controlTcpAddress,
        final ConnectionsManager connectionsManager
    ) {
        final int invokerSocketHandle = connectToInvoker(invokerTcpAddress, connectionsManager);
        final int controlSocketHandle = connectToControl(controlTcpAddress, connectionsManager);

        socketHandleRegistry.registerInstanceSocketHandles(
            invokerTcpAddress,
            invokerSocketHandle,
            controlTcpAddress,
            controlSocketHandle
        );

        sendStatusRequest(controlSocketHandle, connectionsManager);
    }


    private void handleDisconnectInstanceCommand(
        final String invokerTcpAddress,
        final String controlTcpAddress,
        final ConnectionsManager connectionsManager
    ) {
        final int invokerSocketHandle = socketHandleRegistry.getSocketHandleForInvokerTcpAddress(invokerTcpAddress);
        final int controlSocketHandle = socketHandleRegistry.getSocketHandleForControlTcpAddress(controlTcpAddress);

        final UUID instanceUuid = socketHandleRegistry.getInstanceUuidForInvokerSocketHandle(invokerSocketHandle);

        try {
            connectionsManager.closeAndDestroySocketHandle(invokerSocketHandle);
            connectionsManager.closeAndDestroySocketHandle(controlSocketHandle);
        }
        catch (IllegalStateException e) {
            logger.error("Ctrl-bind: Failed to close/destroy ivkr/ctrl socket for instance uuid {} with error: {}", instanceUuid, e);
        }


        final Set<Integer> nodeSocketHandles = socketHandleRegistry.getNodeSocketHandlesForInstanceUuid(instanceUuid);
        nodeSocketHandles.forEach(nodeSocketHandle -> {
            try {
                connectionsManager.closeAndDestroySocketHandle(nodeSocketHandle);
            }
            catch (IllegalStateException e) {
                logger.error("Ctrl-bind: Failed to close/destroy a node socket for instance uuid {} with error: {}", instanceUuid, e);
            }
        });

        socketHandleRegistry.unregisterInstance(instanceUuid);
    }

    private void handleRoutingCommand(
            final RoutingCommand routingCommand,
            final ConnectionsManager connectionsManager
    ) {
        final Action action = routingCommand.action.get();
        final String tcpAddress = routingCommand.tcpAddress.get();
        final UUID instanceUuid = routingCommand.instanceUuid.get();
        final UUID applicationUuid = routingCommand.applicationUuid.get();
        final NodeId nodeId = new NodeId(instanceUuid, applicationUuid);

        // TODO: delete this log
        logger.info("Ctrl-bind: recv rout cmd: {}", action);

        switch (action) {
            case NO_OP: {
                break;
            }
            case BIND_INVOKER: {
                final int invokerSocketHandle = bindInvoker(tcpAddress, connectionsManager);
                if (invokerSocketHandle != SOCKET_HANDLE_NOT_FOUND) {
                    socketHandleRegistry.registerBoundInvokerSocket(tcpAddress, invokerSocketHandle);
                }
                break;
            }
            case UNBIND_INVOKER: {
                final int invokerSocketHandle = unbindInvoker(connectionsManager);
                if (invokerSocketHandle != SOCKET_HANDLE_NOT_FOUND) {
                    socketHandleRegistry.unregisterBoundInvokerSocket();
                }
                break;
            }
            case CONNECT_NODE: {
                final int nodeSocketHandle = connectToNode(nodeId, connectionsManager);
                if (nodeSocketHandle != SOCKET_HANDLE_NOT_FOUND) {
                    socketHandleRegistry.registerNode(nodeId, nodeSocketHandle);
                }
                break;
            }
            case DISCONNECT_NODE: {
                final int nodeSocketHandle = disconnectFromNode(nodeId, connectionsManager);
                if (nodeSocketHandle != SOCKET_HANDLE_NOT_FOUND) {
                    socketHandleRegistry.unregisterNode(nodeId);
                }
                break;
            }
            case BIND_NODE: {
                final int nodeSocketHandle = bindNode(nodeId, connectionsManager);
                if (nodeSocketHandle != SOCKET_HANDLE_NOT_FOUND) {
                    socketHandleRegistry.registerNode(nodeId, nodeSocketHandle);
                }
                break;
            }
            case UNBIND_NODE: {
                final int nodeSocketHandle = unbindNode(nodeId, connectionsManager);
                if (nodeSocketHandle != SOCKET_HANDLE_NOT_FOUND) {
                    socketHandleRegistry.unregisterNode(nodeId);
                }
                break;
            }
            default: {
                logger.warn("Ctrl-bind: Encountered unhandled routing action: {}. Dropping message.", action);
                break;
            }
        }
    }



    //================================================================================
    //
    // Connect/Disconnect/Bind/Unbind
    //
    //================================================================================

    private int connectToInvoker(
            final String invokerTcpAddress,
            final ConnectionsManager connectionsManager
    ) {
        final int socketHandle = connectionsManager.connectToAddressAndBeginPolling(
            invokerTcpAddress,
            DEALER,
            this::handleConnectedInvokerMessage,
            true
                );

        return socketHandle;
    }

    private int bindInvoker(
            final String invokerTcpAddress,
            final ConnectionsManager connectionsManager
    ) {
        final int invokerSocketHandle = connectionsManager.bindToAddressAndBeginPolling(
            invokerTcpAddress,
                DEALER,
                this::handleBoundInvokerMessage,
                true
        );

        return invokerSocketHandle;
    }

    private int connectToControl(
        final String controlTcpAddress,
        final ConnectionsManager connectionsManager
    ) {
        final int controlSocketHandle = connectionsManager.connectToAddressAndBeginPolling(
            controlTcpAddress,
            PUSH,
            this::handleConnectedControlMessage,
            true
        );

        return controlSocketHandle;
    }

    private int unbindInvoker(
            final ConnectionsManager connectionsManager
    ) {
        final int invokerSocketHandle = socketHandleRegistry.getBoundInvokerSocketHandle();
        if (invokerSocketHandle == SOCKET_HANDLE_NOT_FOUND) {
            logger.warn("Ctrl-bind: Received control command to unbind the invoker socket, but a handle is not currently registered. Dropping message.");
            return SOCKET_HANDLE_NOT_FOUND;
        }

        try {
            connectionsManager.closeAndDestroySocketHandle(invokerSocketHandle);
        }
        catch (Exception e) {
            logger.error("Ctrl-bind: Failed to close/destroy ivkr socket {} with error: {}", invokerSocketHandle, e);
            return SOCKET_HANDLE_NOT_FOUND;
        }

        return invokerSocketHandle;
    }

    private int connectToNode(
        final NodeId nodeId,
        final ConnectionsManager connectionsManager
    ) {
        final String nodeAddress;

        final MessageManagerConfiguration.Strategy strategy = messageManagerConfiguration.getStrategy();

        // TODO: maybe rename these to Client vs Instance instead of Multiplex vs Demultiplex
        switch (strategy) {
            case MULTIPLEX:
                nodeAddress = RouteRepresentationUtil.buildMultiplexInprocAddress(nodeId);
                break;
            case DEMULTIPLEX:
                nodeAddress = RouteRepresentationUtil.buildDemultiplexInprocAddress(nodeId);
                break;
            default: {
                logger.warn("Ctrl-bind: Connect node: Encountered unhandled strategy in configuration: {}. Dropping message.", strategy);
                return SOCKET_HANDLE_NOT_FOUND;
            }
        }

        final int socketHandle = connectionsManager.connectToAddressAndBeginPolling(
            nodeAddress,
            DEALER,
            this::handleConnectedNodeMessage,
            false
        );

        return socketHandle;
    }

    private int disconnectFromNode(
        final NodeId nodeId,
        final ConnectionsManager connectionsManager
    ) {
        final int socketHandle = socketHandleRegistry.getSocketHandleForNodeId(nodeId);
        if (socketHandle == SOCKET_HANDLE_NOT_FOUND) {
            return SOCKET_HANDLE_NOT_FOUND;
        }

        try {
            connectionsManager.closeAndDestroySocketHandle(socketHandle);
        }
        catch (Exception e) {
            logger.error("Ctrl-bind: Disconnect node: Failed to close/destroy connected node socket handle {} with error: {}", socketHandle, e);
        }

        return socketHandle;
    }

    private int bindNode(
        final NodeId nodeId,
        final ConnectionsManager connectionsManager
    ) {
        final String inprocAddress;

        final MessageManagerConfiguration.Strategy strategy = messageManagerConfiguration.getStrategy();

        // TODO: see the notes above in connectToNode

        switch (strategy) {
            case MULTIPLEX:
                inprocAddress = RouteRepresentationUtil.buildMultiplexInprocAddress(nodeId);
                break;
            case DEMULTIPLEX:
                inprocAddress = RouteRepresentationUtil.buildDemultiplexInprocAddress(nodeId);
                break;
            default: {
                logger.warn("Ctrl-bind: Bind node: Encountered unhandled strategy in configuration: {}. Dropping message.", strategy);
                return SOCKET_HANDLE_NOT_FOUND;
            }
        }

        final int socketHandle = connectionsManager.bindToAddressAndBeginPolling(
            inprocAddress,
            ROUTER,
            this::handleBoundNodeMessage,
            false
        );

        return socketHandle;
    }

    private int unbindNode(
        final NodeId nodeId,
        final ConnectionsManager connectionsManager
    ) {
        final int socketHandle = socketHandleRegistry.getSocketHandleForNodeId(nodeId);
        if (socketHandle == SOCKET_HANDLE_NOT_FOUND) {
            return SOCKET_HANDLE_NOT_FOUND;
        }

        try {
            connectionsManager.closeAndDestroySocketHandle(socketHandle);
        }
        catch (Exception e) {
            logger.error("Ctrl-bind: Unbind node: Failed to close/destroy bound node socket handle {} with error: {}", socketHandle, e);
        }

        return socketHandle;
    }



    //================================================================================
    //
    // Handle tcp/inproc received messages
    //
    //================================================================================

    private void handleBoundInvokerMessage(
        final int socketHandle,
        final ZMsg msg,
        final ConnectionsManager connectionsManager
    ) {
        final RoutingHeader routingHeader = RouteRepresentationUtil.getAndStripRoutingHeader(msg);
        final UUID instanceUuid = routingHeader.instanceUuid.get();
        final UUID applicationUuid = routingHeader.applicationUuid.get();
        final NodeId nodeId = new NodeId(instanceUuid, applicationUuid);
        final Status status = routingHeader.status.get();

        if (status == CONTINUE) {
            final int nodeSocketHandle;
            if (socketHandleRegistry.hasNodeId(nodeId)) {
                nodeSocketHandle = socketHandleRegistry.getSocketHandleForNodeId(nodeId);
            }
            else {
                nodeSocketHandle = connectToNode(nodeId, connectionsManager);
                if (nodeSocketHandle != SOCKET_HANDLE_NOT_FOUND) {
                    socketHandleRegistry.registerNode(nodeId, nodeSocketHandle);
                }
            }

            if (nodeSocketHandle != SOCKET_HANDLE_NOT_FOUND) {
                connectionsManager.sendMsgToSocketHandle(nodeSocketHandle, msg);
            }
            else {
                logger.error("Ivkr-bind: Failed to handle invoker bind msg for NodeId {}. Dropping message.",
                    status, nodeId);
            }
        } else {
            logger.error("Ivkr-bind: Failed to handle invoker bind msg with status {} for NodeId {}. Dropping message.",
                status, nodeId);
        }
    }

    public void handleBoundControlMessage(
        final int socketHandle,
        final ZMsg msg,
        final ConnectionsManager connectionsManager
    ) {
        final byte[] commandPreambleBytes = msg.pop().getData();
        final CommandPreamble preamble = CommandPreambleFromBytes(commandPreambleBytes);

        final CommandType commandType = preamble.commandType.get();

        // TODO: make sure to remove logs
        logger.info("Ctrl-bind: Recv ctrl msg: {}", commandType);

        switch(commandType) {
            case STATUS_REQUEST:    // we received a request for current status
                sendStatusResponse(socketHandle, connectionsManager);
                break;
            case ROUTING_COMMAND:   // we have received a command to open/close another channel (inproc or backend)
                // conditionally send back msg ack (e.g. if we are demultiplexer)
                if (messageManagerConfiguration.isShouldSendRoutingCommandAcknowledgement()) {
                    sendRoutingCommandAcknowledgement(socketHandle, connectionsManager);
                }

                // so, convert the ZMsg into a RoutingCommand
                final byte[] routingCommandBytes = msg.pop().getData();
                final RoutingCommand routingCommand = RoutingCommandFromBytes(routingCommandBytes);

                // and handle the formed RoutingCommand
                handleRoutingCommand(routingCommand, connectionsManager);
                break;
            case INSTANCE_CONNECTION_COMMAND:
                final byte[] instanceConnectionCommandBytes = msg.pop().getData();
                final InstanceConnectionCommand instanceConnectionCommand = InstanceConnectionCommandFromBytes(instanceConnectionCommandBytes);
                handleInstanceConnectionCommand(instanceConnectionCommand, connectionsManager);
                break;
            case INSTANCE_UUID_LIST_REQUEST:
                sendInstanceUuidListResponse(socketHandle, connectionsManager);
                break;
            default:
                logger.error("Ctrl-bind: Unexpected command: {}", preamble.commandType.get());
        }
    }

    private void handleConnectedInvokerMessage(
        final int socketHandle,
        final ZMsg msg,
        final ConnectionsManager connectionsManager
    ) {
        final RoutingHeader routingHeader = RouteRepresentationUtil.getAndStripRoutingHeader(msg);
        final UUID instanceUuid = routingHeader.instanceUuid.get();
        final UUID applicationUuid = routingHeader.applicationUuid.get();
        final NodeId nodeId = new NodeId(instanceUuid, applicationUuid);
        final Status status = routingHeader.status.get();

        if (status == CONTINUE) {
            final int nodeSocketHandle = socketHandleRegistry.getSocketHandleForNodeId(nodeId);

            if (nodeSocketHandle != SOCKET_HANDLE_NOT_FOUND) {
                logger.warn("Ivkr-conn: Node conn not established for NodeId {}. Dropping message.", nodeId);
                return;
            }

            connectionsManager.sendMsgToSocketHandle(nodeSocketHandle, msg);
        }
        else {
            logger.error("Ivkr-conn: Failed to handle invoker conn msg with status {} for NodeId {}. Dropping message.",
                    status, nodeId);
        }
    }

    private void handleConnectedControlMessage(
        final int socketHandle,
        final ZMsg msg,
        final ConnectionsManager connectionsManager
    ) {
        final byte[] commandPreambleBytes = msg.pop().getData();
        final CommandPreamble preamble = CommandPreambleFromBytes(commandPreambleBytes);

        final CommandType commandType = preamble.commandType.get();

        // TODO: make sure to remove logs
        logger.info("Recv ctrl msg: {}", commandType);

        switch (commandType) {
            case STATUS_RESPONSE:
                final byte[] statusResponseBytes = msg.pop().getData();
                final StatusResponse statusResponse = StatusResponseFromBytes(statusResponseBytes);
                handleStatusResponse(socketHandle, statusResponse);
                break;
            default:
                logger.error("Ctrl-conn: Unexpected command: {}", preamble.commandType.get());
                break;
        }
    }

    private void handleConnectedNodeMessage(
            final int socketHandle,
            final ZMsg msg,
            final ConnectionsManager connectionsManager
    ) {
        final NodeId nodeId = socketHandleRegistry.getNodeIdForSocketHandle(socketHandle);

        if (nodeId == null) {
            logger.error("Node-conn: NodeId not found for socket handle {}. Dropping message.", socketHandle);
            return;
        }

        final RoutingHeader routingHeader = new RoutingHeader();
        routingHeader.status.set(CONTINUE);
        routingHeader.instanceUuid.set(nodeId.getInstanceUuid());
        routingHeader.applicationUuid.set(nodeId.getApplicationUuid());

        RouteRepresentationUtil.insertRoutingHeader(msg, routingHeader);

        final int invokerSocketHandle = socketHandleRegistry.getInvokerSocketHandleForInstanceUuid(nodeId.getInstanceUuid());

        connectionsManager.sendMsgToSocketHandle(invokerSocketHandle, msg);
    }

    private void handleBoundNodeMessage(
            final int socketHandle,
            final ZMsg msg,
            final ConnectionsManager connectionsManager
    ) {
        final NodeId nodeId = socketHandleRegistry.getNodeIdForSocketHandle(socketHandle);

        if (nodeId == null) {
            logger.error("Node-bind: NodeId not found for socket handle {}. Dropping message.", socketHandle);
            return;
        }

        final RoutingHeader routingHeader = new RoutingHeader();
        routingHeader.status.set(CONTINUE);
        routingHeader.instanceUuid.set(nodeId.getInstanceUuid());
        routingHeader.applicationUuid.set(nodeId.getApplicationUuid());

        RouteRepresentationUtil.insertRoutingHeader(msg, routingHeader);

        final int invokerSocketHandle = socketHandleRegistry.getInvokerSocketHandleForInstanceUuid(nodeId.getInstanceUuid());

        connectionsManager.sendMsgToSocketHandle(invokerSocketHandle, msg);
    }



    @Override
    public void close() {
        socketHandleRegistry.close();
    }

    public static final class MessageManagerConfiguration {
        private final boolean shouldSendRoutingCommandAcknowledgement;
        private final UUID instanceUuid;
        private final Strategy strategy;

        public MessageManagerConfiguration(
                final Strategy strategy,
                final UUID instanceUuid,
                final boolean shouldSendRoutingCommandAcknowledgement
        ) {
            this.strategy = strategy;
            this.instanceUuid = instanceUuid;
            this.shouldSendRoutingCommandAcknowledgement = shouldSendRoutingCommandAcknowledgement;
        }

        public boolean isShouldSendRoutingCommandAcknowledgement() {
            return shouldSendRoutingCommandAcknowledgement;
        }

        public Strategy getStrategy() {
            return strategy;
        }

        public UUID getInstanceUuid() {
            return instanceUuid;
        }

        public enum Strategy {
            MULTIPLEX,

            DEMULTIPLEX,
        }
    }
}
