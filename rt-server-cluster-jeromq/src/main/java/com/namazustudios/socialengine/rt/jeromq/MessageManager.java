package com.namazustudios.socialengine.rt.jeromq;


import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMsg;

import java.util.UUID;

import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandPreambleFromBytes;
import static com.namazustudios.socialengine.rt.remote.RoutingCommand.RoutingCommandFromBytes;
import static com.namazustudios.socialengine.rt.remote.StatusResponse.buildStatusResponse;
import static com.namazustudios.socialengine.rt.remote.InstanceConnectionCommand.InstanceConnectionCommandFromBytes;
import com.namazustudios.socialengine.rt.remote.RoutingCommand.Action;
import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType;
import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType.ROUTING_COMMAND_ACK;
import static com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType.STATUS_RESPONSE;
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
            default:
                logger.error("Unexpected command: {}", preamble.commandType.get());
        }
    }

    private void sendStatusResponse(final int socketHandle, final ConnectionsManager connectionsManager) {
        // build the response with status data
        final StatusResponse statusResponse = buildStatusResponse(messageManagerConfiguration.getInstanceUuid());
        final ZMsg responseMsg = buildControlMsg(STATUS_RESPONSE, statusResponse.getByteBuffer());

        // and send the response back over the same pipe
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
                break;
            }
            case DISCONNECT: {

            }
            default: {
                logger.warn("Encountered unhandled instance connection action: {}. Dropping message.", action);
                break;
            }
        }
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
            case CONNECT_TCP: {
                connectToTcpAddress(tcpAddress, connectionsManager);
                break;
            }
            case DISCONNECT_TCP: {
                disconnectFromTcpAddress(tcpAddress, connectionsManager);
                break;
            }
            case BIND_TCP: {
                bindToTcpAddress(tcpAddress, connectionsManager);
                break;
            }
            case UNBIND_TCP: {
                unbindFromTcpAddress(tcpAddress, connectionsManager);
                break;
            }
            case CONNECT_INPROC: {
                connectToInprocAddress(tcpAddress, inprocIdentifier, connectionsManager);
                break;
            }
            case DISCONNECT_INPROC: {
                disconnectFromInprocAddress(inprocIdentifier, connectionsManager);
                break;
            }
            case BIND_INPROC: {
                bindToInprocAddress(tcpAddress, inprocIdentifier, connectionsManager);
                break;
            }
            case UNBIND_INPROC: {
                unbindFromInprocAddress(inprocIdentifier, connectionsManager);
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
        socketHandleRegistry.registerTcpSocketHandle(socketHandle, tcpAddress);

        return socketHandle;
    }

    private void disconnectFromTcpAddress(
            final String tcpAddress,
            final ConnectionsManager connectionsManager
    ) {
        final int socketHandle = socketHandleRegistry.getTcpSocketHandle(tcpAddress);
        if (socketHandle < 0) {
            return;
        }

        try {
            connectionsManager.closeAndDestroySocketHandle(socketHandle);
            socketHandleRegistry.unregisterTcpSocketHandle(socketHandle);
        }
        catch (Exception e) {
            logger.error("Failed to close/destroy connected tcp socket handle {} with error: {}", socketHandle, e);
        }
    }

    private int bindToTcpAddress(
            final String tcpAddress,
            final ConnectionsManager connectionsManager
    ) {
        final int socketHandle = connectionsManager.bindToAddressAndBeginPolling(
                tcpAddress,
                DEALER,
                this::handleBoundTcpMessage,
                true
        );
        socketHandleRegistry.registerTcpSocketHandle(socketHandle, tcpAddress);

        return socketHandle;
    }

    private void unbindFromTcpAddress(
            final String tcpAddress,
            final ConnectionsManager connectionsManager
    ) {
        final int socketHandle = socketHandleRegistry.getTcpSocketHandle(tcpAddress);
        if (socketHandle < 0) {
            return;
        }

        try {
            connectionsManager.closeAndDestroySocketHandle(socketHandle);
            socketHandleRegistry.unregisterTcpSocketHandle(socketHandle);
        }
        catch (Exception e) {
            logger.error("Failed to close/destroy bound tcp socket handle {} with error: {}", socketHandle, e);
        }
    }

    private int connectToInprocAddress(
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

    private void disconnectFromInprocAddress(
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

    private int bindToInprocAddress(
            final String tcpAddress,
            final UUID inprocIdentifier,
            final ConnectionsManager connectionsManager
    ) {
        final String inprocAddress;

        final MessageManagerConfiguration.Strategy strategy = messageManagerConfiguration.getStrategy();

        // TODO: see the notes above in connectToInprocAddress

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

    private void unbindFromInprocAddress(
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
                inprocSocketHandle = connectToInprocAddress(tcpAddress, inprocIdentifier, connectionsManager);
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
