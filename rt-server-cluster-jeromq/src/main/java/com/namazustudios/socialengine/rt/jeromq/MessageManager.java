package com.namazustudios.socialengine.rt.jeromq;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.UUID;

import com.namazustudios.socialengine.rt.jeromq.RoutingCommand.Action;
import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType.ROUTING_COMMAND_ACK;
import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType.STATUS_RESPONSE;
import static com.namazustudios.socialengine.rt.jeromq.ControlMessageBuilder.buildControlMsg;
import static org.zeromq.ZMQ.DEALER;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMQ.ROUTER;

/**
 * Handles ZMQ messages. This is not thread-safe and is meant to be self-contained, i.e. method calls within
 * this module should only originate from received ZMQ messages that this module consumes (or from high-level close
 * commands from AutoClosable/thread shutdown).
 */
public class MessageManager implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MessageManager.class);

    private final MessageHandlerConfiguration messageHandlerConfiguration;

    private final SocketHandleRegistry socketHandleRegistry = new SocketHandleRegistry();

    public MessageManager(final MessageHandlerConfiguration messageHandlerConfiguration) {
        this.messageHandlerConfiguration = messageHandlerConfiguration;
    }

    public void handleControlMessage(
            final int socketHandle,
            final ZMsg msg,
            final ConnectionsManager connectionsManager
    ) {
        // TODO: make sure to remove logs
        logger.info("Recv control msg");

        final CommandPreamble preamble = new CommandPreamble();

        preamble.getByteBuffer().put(msg.pop().getData());

        switch(preamble.commandType.get()) {
            case STATUS_REQUEST:    // we received a request for current status
                sendStatusResponse(socketHandle, connectionsManager);
                break;
            case ROUTING_COMMAND:   // we have received a command to open/close another channel (inproc or backend)
                // conditionally send back msg ack (e.g. if we are demultiplexer)
                if (messageHandlerConfiguration.isShouldSendRoutingCommandAcknowledgement()) {
                    sendRoutingCommandAcknowledgement(socketHandle, connectionsManager);
                }

                // so, convert the ZMsg into a RoutingCommand
                final RoutingCommand routingCommand = new RoutingCommand();
                routingCommand.getByteBuffer().put(msg.pop().getData());

                handleRoutingCommand(routingCommand, connectionsManager);
                break;
            default:
                logger.error("Unexpected command: {}", preamble.commandType.get());
        }
    }

    private void sendStatusResponse(final int socketHandle, final ConnectionsManager connectionsManager) {
        // build the response with status data
        final StatusResponse statusResponse = new StatusResponse();
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

    private void handleRoutingCommand(
            final RoutingCommand routingCommand,
            final ConnectionsManager connectionsManager
    ) {
        final Action action = routingCommand.action.get();
        final String tcpAddress = routingCommand.tcpAddress.get();
        final UUID inprocIdentifier = routingCommand.inprocIdentifier.get();

        switch (action) {
            case CONNECT_TCP: {
                connectToTcpAddress(tcpAddress, connectionsManager);
                break;
            }
            case DISCONNECT_TCP: {
                disconnectFromTcpAddress(tcpAddress, connectionsManager);
                break;
            }
            case BIND_INPROC: {
                bindInprocSocket(inprocIdentifier, connectionsManager);
                break;
            }
            case UNBIND_INPROC: {
                unbindInprocSocket(inprocIdentifier, connectionsManager);
                break;
            }
            case CONNECT_INPROC: {
                connectToInprocSocket(inprocIdentifier, connectionsManager);
                break;
            }
            case DISCONNECT_INPROC: {
                disconnectFromInprocSocket(inprocIdentifier, connectionsManager);
                break;
            }
        }
    }

    private void connectToTcpAddress(
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

    private void bindInprocSocket(
            final UUID inprocIdentifier,
            final ConnectionsManager connectionsManager
    ) {
        final String inprocAddress = RouteRepresentationUtil.buildConnectInprocAddress(inprocIdentifier);
        final int socketHandle = connectionsManager.bindToAddressAndBeginPolling(
                inprocAddress,
                ROUTER,
                this::handleBoundInprocMessage,
                false
        );
        socketHandleRegistry.registerInprocSocketHandle(socketHandle, inprocIdentifier);
    }

    private void unbindInprocSocket(
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

    private void connectToInprocSocket(
            final UUID inprocIdentifier,
            final ConnectionsManager connectionsManager
    ) {
        final String inprocAddress = RouteRepresentationUtil.buildConnectInprocAddress(inprocIdentifier);
        final int socketHandle = connectionsManager.connectToAddressAndBeginPolling(
                inprocAddress,
                DEALER,
                this::handleConnectedInprocMessage,
                false
        );
        socketHandleRegistry.registerInprocSocketHandle(socketHandle, inprocIdentifier);
    }

    private void disconnectFromInprocSocket(
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

    private void handleConnectedTcpMessage(
            final int socketHandle,
            final ZMsg msg,
            final ConnectionsManager connectionsManager
    ) {

    }

    private void handleBoundInprocMessage(
            final int socketHandle,
            final ZMsg msg,
            final ConnectionsManager connectionsManager
    ) {

    }

    private void handleConnectedInprocMessage(
            final int socketHandle,
            final ZMsg msg,
            final ConnectionsManager connectionsManager
    ) {

    }

    @Override
    public void close() {

    }

    public static final class MessageHandlerConfiguration {
        private final boolean shouldSendRoutingCommandAcknowledgement;

        public MessageHandlerConfiguration(final boolean shouldSendRoutingCommandAcknowledgement) {
            this.shouldSendRoutingCommandAcknowledgement = shouldSendRoutingCommandAcknowledgement;
        }

        public boolean isShouldSendRoutingCommandAcknowledgement() {
            return shouldSendRoutingCommandAcknowledgement;
        }
    }
}
