package com.namazustudios.socialengine.rt.jeromq;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.UUID;

import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType.STATUS_RESPONSE;
import static com.namazustudios.socialengine.rt.jeromq.ControlMessageBuilder.buildControlMessage;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;

/**
 * Handles ZMQ messages. This is not thread-safe and is meant to be self-contained, i.e. method calls within
 * this module should only originate from received ZMQ messages that this module consumes (or from high-level close
 * commands from AutoClosable/thread shutdown).
 */
public class MessageHandler implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

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
                handleStatusRequest(socketHandle, connectionsManager);
                break;
            case ROUTING_COMMAND:   // we have received a command to open/close another channel (inproc or backend)
                // so, convert the Zmsg into a RoutingCommand
                final RoutingCommand routingCommand = new RoutingCommand();
                routingCommand.getByteBuffer().put(msg.pop().getData());

                handleRoutingCommand(routingCommand, connectionsManager);
                break;
            default:
                logger.error("Unexpected command: {}", preamble.commandType.get());
        }
    }

    private void handleStatusRequest(final int socketHandle, final ConnectionsManager connectionsManager) {
        // build the response with status data
        final StatusResponse statusResponse = new StatusResponse();
        final ZMsg responseMsg = buildControlMessage(STATUS_RESPONSE, statusResponse.getByteBuffer());

        // and send the response back over the same pipe
        connectionsManager.sendMsgToSocketHandle(socketHandle, responseMsg);
    }

    private void handleRoutingCommand(RoutingCommand routingCommand, ConnectionsManager connectionsManager) {
        final RoutingCommand.Action action = routingCommand.action.get();
        final String backendAddress = routingCommand.backendAddress.get();
        final UUID inprocIdentifier = routingCommand.inprocIdentifier.get();

        switch (action) {
            case OPEN_BACKEND:
                openBackendChannel(backendAddress, connectionsManager);
                break;
            case CLOSE_BACKEND:
                closeBackendChannel(backendAddress, connectionsManager);
                break;
            case OPEN_INPROC:
                openInprocChannel(backendAddress, inprocIdentifier);
                break;
            case CLOSE_INPROC:
                closeInprocChannel(backendAddress, inprocIdentifier);
                break;
        }
    }

    public int openBackendChannel(final String backendAddress) {
        if (backendAddressesToSocketHandles.containsKey(backendAddress)) {
            final int backendSocketHandle = backendAddressesToSocketHandles.get(backendAddress);
            return backendSocketHandle;
        }

        final ZMQ.Socket backendSocket = backendConnector.apply(backendAddress);
        final int backendSocketHandle = getPoller().register(backendSocket, POLLIN | POLLERR);

        backendSocketHandlesToAddresses.put(backendSocketHandle, backendAddress);
        backendAddressesToSocketHandles.put(backendAddress, backendSocketHandle);

        final InprocChannelTable backendInprocChannelTable = new InprocChannelTable(getzContext(), getPoller(), inprocConnector);
        backendAddressesToInprocChannelTables.put(backendAddress, backendInprocChannelTable);

        final MonitorThread monitorThread = new MonitorThread(
                getClass().getSimpleName(),
                logger,
                getzContext(),
                backendSocket
        );

        monitorThread.start();

        backendAddressesToMonitorThreads.put(backendAddress, monitorThread);

        return backendSocketHandle;
    }

    public int openInprocChannel(final String backendAddress, final UUID inprocIdentifier) {
        final InprocChannelTable backendInprocChannelTable = getInprocChannelTable(backendAddress);

        if (backendInprocChannelTable == null) {
            return -1;
        }

        final int inprocSocketHandle = backendInprocChannelTable.open(inprocIdentifier);

        final int backendSocketHandle = getBackendSocketHandleForBackendAddress(backendAddress);

        inprocSocketHandlesToBackendSocketHandles.put(inprocSocketHandle, backendSocketHandle);
        inprocSocketHandlesToBackendAddresses.put(inprocSocketHandle, backendAddress);

        return inprocSocketHandle;
    }

    public void closeBackendChannel(final String backendAddress) {

        final Integer backendSocketHandle = backendAddressesToSocketHandles.remove(backendAddress);

        if (backendSocketHandle != null && backendSocketHandlesToAddresses.remove(backendAddress) != null) {
            backendAddressesToInprocChannelTables.get(backendAddress).close();
            backendAddressesToInprocChannelTables.remove(backendAddress);

            final ZMQ.Socket socket = getPoller().getSocket(backendSocketHandle);
            closeBackendSocket(socket);
        }

    }

    public void closeInprocChannel(String backendAddress, UUID inprocIdentifier) {
        if (!backendAddressesToInprocChannelTables.containsKey(backendAddress)) {
            return;
        }

        final InprocChannelTable inprocChannelTable = backendAddressesToInprocChannelTables.get(backendAddress);
        final int inprocSocketHandle = inprocChannelTable.getInprocSocketHandle(inprocIdentifier);
        inprocSocketHandlesToBackendSocketHandles.remove(inprocSocketHandle);
        inprocSocketHandlesToBackendAddresses.remove(inprocSocketHandle);

        inprocChannelTable.close(inprocIdentifier);
    }

    @Override
    public void close() {

    }
}
