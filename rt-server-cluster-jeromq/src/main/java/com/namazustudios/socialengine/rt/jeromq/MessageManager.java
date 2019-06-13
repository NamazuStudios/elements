package com.namazustudios.socialengine.rt.jeromq;


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



    //================================================================================
    //
    // Handle Control Message
    //
    //================================================================================

    public void handleControlMessage(
            final int socketHandle,
            final ZMsg msg,
            final ConnectionsManager connectionsManager
    ) {
        final byte[] commandPreambleBytes = msg.pop().getData();
        final CommandPreamble preamble = CommandPreambleFromBytes(commandPreambleBytes);

        final CommandType commandType = preamble.commandType.get();

        // TODO: make sure to remove logs
        logger.info("Recv ctrl msg: {}", commandType);

        switch(commandType) {
            case STATUS_REQUEST:    // we received a request for current status
                sendStatusResponse(socketHandle, connectionsManager);
                break;
            case STATUS_RESPONSE:
                final byte[] statusResponseBytes = msg.pop().getData();
                final StatusResponse statusResponse = StatusResponseFromBytes(statusResponseBytes);
                handleStatusResponse(socketHandle, statusResponse);
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
                logger.error("Unexpected command: {}", preamble.commandType.get());
        }
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
        logger.info("Recv inst conn cmd: {}, invk: {}, ctrl: {}", action, invokerTcpAddress, controlTcpAddress);

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
                logger.warn("Encountered unhandled instance connection action: {}. Dropping message.", action);
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
        final int invokerSocketHandle = connectToTcpAddress(invokerTcpAddress, connectionsManager);
        final int controlSocketHandle = connectToTcpAddress(controlTcpAddress, connectionsManager);

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
            logger.error("Failed to close/destroy connected tcp socket handle for instance uuid {} with error: {}", instanceUuid, e);
        }


        final Set<Integer> nodeSocketHandles = socketHandleRegistry.getNodeSocketHandlesForInstanceUuid(instanceUuid);
        nodeSocketHandles.forEach(nodeSocketHandle -> {
            try {
                connectionsManager.closeAndDestroySocketHandle(nodeSocketHandle);
            }
            catch (IllegalStateException e) {
                logger.error("Failed to close/destroy connected inproc socket handle for instance uuid {} with error: {}", instanceUuid, e);
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
        final UUID inprocIdentifier = routingCommand.inprocIdentifier.get();

        // TODO: delete this log
        logger.info("Recv rout cmd: {}", action);

        switch (action) {
            case NO_OP: {
                break;
            }
            case BIND_INVOKER: {
                bindInvoker(tcpAddress, connectionsManager);
                break;
            }
            case UNBIND_INVOKER: {
                unbindInvoker(connectionsManager);
                break;
            }
            case CONNECT_NODE: {
                connectToNode(tcpAddress, inprocIdentifier, connectionsManager);
                break;
            }
            case DISCONNECT_NODE: {
                disconnectFromNode(inprocIdentifier, connectionsManager);
                break;
            }
            case BIND_NODE: {
                bindNode(tcpAddress, inprocIdentifier, connectionsManager);
                break;
            }
            case UNBIND_NODE: {
                unbindNode(inprocIdentifier, connectionsManager);
                break;
            }
            default: {
                logger.warn("Encountered unhandled routing action: {}. Dropping message.", action);
                break;
            }
        }
    }



    //================================================================================
    //
    // Connect/Disconnect/Bind/Unbind
    //
    //================================================================================

    private int connectToTcpAddress(
            final String tcpAddress,
            final ConnectionsManager connectionsManager
    ) {
        final int socketHandle = connectionsManager.connectToAddressAndBeginPolling(
                tcpAddress,
                DEALER,
                this::handleConnectedTcpMessage,
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
                this::handleBoundTcpMessage,
                true
        );
        socketHandleRegistry.registerBoundInvokerSocket(invokerTcpAddress, invokerSocketHandle);

        return invokerSocketHandle;
    }

    private void unbindInvoker(
            final ConnectionsManager connectionsManager
    ) {
        final int invokerSocketHandle = socketHandleRegistry.getBoundInvokerSocketHandle();
        if (invokerSocketHandle == SOCKET_HANDLE_NOT_FOUND) {
            logger.warn("Received control command to unbind the invoker socket, but a handle is not currently registered. Dropping message.");
            return;
        }

        try {
            connectionsManager.closeAndDestroySocketHandle(invokerSocketHandle);
        }
        catch (Exception e) {
            logger.error("Failed to close/destroy bound tcp socket handle {} with error: {}", invokerSocketHandle, e);
            return;
        }

        socketHandleRegistry.unregisterBoundInvokerSocket();
    }

    private int connectToNode(
            final String tcpAddress,
            final UUID inprocIdentifier,
            final ConnectionsManager connectionsManager
    ) {
        final String inprocAddress;

        final MessageManagerConfiguration.Strategy strategy = messageManagerConfiguration.getStrategy();

        // TODO:
        //  1) we need to have a tcp->instance uuid registry
        //  2) we get instance uuid from the given tcpAddr
        //  3) we build nodeid accordingly
        //  alternatively.... we just transmit the full nodeid string in the RoutingCommand, and parse it here. when the
        //  routingcommand is going to be transmitted, we look at the instance uuid of the destination for a given nodeid,
        //  look up the tcpAddr at that time, and then stringify the nodeid when we pass it into the routing command builder.
        //  we could also utilize this approach to see if the instanceuuid == local instance uuid and then invoke locally if so

        // TODO: maybe rename these to Client vs Instance instead of Multiplex vs Demultiplex
        switch (strategy) {
            case MULTIPLEX:
                inprocAddress = RouteRepresentationUtil.buildMultiplexInprocAddress(inprocIdentifier);
                break;
            case DEMULTIPLEX:
                inprocAddress = RouteRepresentationUtil.buildDemultiplexInprocAddress(inprocIdentifier);
                break;
            default: {
                logger.warn("Connect inproc: Encountered unhandled strategy in configuration: {}. Dropping message.", strategy);
                return -1;
            }
        }

        final int socketHandle = connectionsManager.connectToAddressAndBeginPolling(
                inprocAddress,
                DEALER,
                this::handleConnectedInprocMessage,
                false
        );
        socketHandleRegistry.registerInprocSocketHandle(socketHandle, tcpAddress, inprocIdentifier);

        return socketHandle;
    }

    private void disconnectFromNode(
            final UUID inprocIdentifier,
            final ConnectionsManager connectionsManager
    ) {
        final int socketHandle = socketHandleRegistry.getInprocSocketHandle(inprocIdentifier);
        if (socketHandle < 0) {
            return;
        }

        try {
            connectionsManager.closeAndDestroySocketHandle(socketHandle);
            socketHandleRegistry.unregisterInprocSocketHandle(socketHandle);
        }
        catch (Exception e) {
            logger.error("Failed to close/destroy connected inproc socket handle {} with error: {}", socketHandle, e);
        }
    }

    private int bindNode(
            final String tcpAddress,
            final UUID inprocIdentifier,
            final ConnectionsManager connectionsManager
    ) {
        final String inprocAddress;

        final MessageManagerConfiguration.Strategy strategy = messageManagerConfiguration.getStrategy();

        // TODO: see the notes above in connectToNode

        switch (strategy) {
            case MULTIPLEX:
                inprocAddress = RouteRepresentationUtil.buildMultiplexInprocAddress(inprocIdentifier);
                break;
            case DEMULTIPLEX:
                inprocAddress = RouteRepresentationUtil.buildDemultiplexInprocAddress(inprocIdentifier);
                break;
            default: {
                logger.warn("Bind inproc: Encountered unhandled strategy in configuration: {}. Dropping message.", strategy);
                return -1;
            }
        }

        final int socketHandle = connectionsManager.bindToAddressAndBeginPolling(
                inprocAddress,
                ROUTER,
                this::handleBoundInprocMessage,
                false
        );
        socketHandleRegistry.registerInprocSocketHandle(socketHandle, tcpAddress, inprocIdentifier);

        return socketHandle;
    }

    private void unbindNode(
            final UUID inprocIdentifier,
            final ConnectionsManager connectionsManager
    ) {
        final int socketHandle = socketHandleRegistry.getInprocSocketHandle(inprocIdentifier);
        if (socketHandle < 0) {
            return;
        }

        try {
            connectionsManager.closeAndDestroySocketHandle(socketHandle);
            socketHandleRegistry.unregisterInprocSocketHandle(socketHandle);
        }
        catch (Exception e) {
            logger.error("Failed to close/destroy bound inproc socket handle {} with error: {}", socketHandle, e);
        }
    }



    //================================================================================
    //
    // Handle tcp/inproc received messages
    //
    //================================================================================

    private void handleConnectedTcpMessage(
            final int socketHandle,
            final ZMsg msg,
            final ConnectionsManager connectionsManager
    ) {
        final RoutingHeader routingHeader = RouteRepresentationUtil.getAndStripRoutingHeader(msg);
        final UUID inprocIdentifier = routingHeader.inprocIdentifier.get();
        final Status status = routingHeader.status.get();

        if (status == CONTINUE) {
            final int inprocSocketHandle = socketHandleRegistry.getInprocSocketHandle(inprocIdentifier);

            if (inprocSocketHandle < 0) {
                logger.warn("Socket not established for inproc identifier {}. Dropping message.", inprocIdentifier);
                return;
            }

            connectionsManager.sendMsgToSocketHandle(inprocSocketHandle, msg);
        }
        else {
            logger.error("Failed to handle tcp conn msg with status {} for inproc identifier {}. Dropping message.",
                    status, inprocIdentifier);
        }
    }

    private void handleBoundTcpMessage(
            final int socketHandle,
            final ZMsg msg,
            final ConnectionsManager connectionsManager
    ) {
        final RoutingHeader routingHeader = RouteRepresentationUtil.getAndStripRoutingHeader(msg);
        final String tcpAddress = routingHeader.tcpAddress.get();
        final UUID inprocIdentifier = routingHeader.inprocIdentifier.get();
        final Status status = routingHeader.status.get();

        if (status == CONTINUE) {
            final int inprocSocketHandle;
            if (socketHandleRegistry.hasInprocIdentifier(inprocIdentifier)) {
                inprocSocketHandle = socketHandleRegistry.getInprocSocketHandle(inprocIdentifier);
            }
            else {
                inprocSocketHandle = connectToNode(tcpAddress, inprocIdentifier, connectionsManager);
            }

            connectionsManager.sendMsgToSocketHandle(inprocSocketHandle, msg);
        } else {
            logger.error("Failed to handle tcp bind msg with status {} for inproc identifier {}. Dropping message.",
                    status, inprocIdentifier);
        }
    }

    private void handleConnectedInprocMessage(
            final int socketHandle,
            final ZMsg msg,
            final ConnectionsManager connectionsManager
    ) {
        final UUID inprocIdentifier = socketHandleRegistry.getInprocIdentifier(socketHandle);
        final String tcpAddress = socketHandleRegistry.getTcpAddressForInprocIdentifier(inprocIdentifier);

        final RoutingHeader routingHeader = new RoutingHeader();
        routingHeader.status.set(CONTINUE);
        routingHeader.tcpAddress.set(tcpAddress);
        routingHeader.inprocIdentifier.set(inprocIdentifier);

        RouteRepresentationUtil.insertRoutingHeader(msg, routingHeader);

        final int tcpSocketHandle = socketHandleRegistry.getTcpSocketHandle(tcpAddress);

        connectionsManager.sendMsgToSocketHandle(tcpSocketHandle, msg);
    }

    private void handleBoundInprocMessage(
            final int socketHandle,
            final ZMsg msg,
            final ConnectionsManager connectionsManager
    ) {
        final UUID inprocIdentifier = socketHandleRegistry.getInprocIdentifier(socketHandle);
        final String tcpAddress = socketHandleRegistry.getTcpAddressForInprocIdentifier(inprocIdentifier);

        final RoutingHeader routingHeader = new RoutingHeader();
        routingHeader.status.set(CONTINUE);
        routingHeader.tcpAddress.set(tcpAddress);
        routingHeader.inprocIdentifier.set(inprocIdentifier);

        RouteRepresentationUtil.insertRoutingHeader(msg, routingHeader);

        final int tcpSocketHandle = socketHandleRegistry.getTcpSocketHandle(tcpAddress);

        connectionsManager.sendMsgToSocketHandle(tcpSocketHandle, msg);
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
