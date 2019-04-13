package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.jeromq.*;
import com.namazustudios.socialengine.rt.remote.RoutingHeader;
import com.namazustudios.socialengine.rt.util.SyncWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import java.util.*;

import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType.STATUS_RESPONSE;
import static com.namazustudios.socialengine.rt.jeromq.JeroMQSocketHost.send;
import static com.namazustudios.socialengine.rt.remote.RoutingHeader.Status.CONTINUE;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.*;
import static zmq.ZError.EHOSTUNREACH;

/**
 * Threaded manager for a multiplexed ZMQ connection.
 */
public class JeroMQMultiplexedConnectionRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQMultiplexedConnectionRunnable.class);

    private final String controlAddress;

    private final ZContext zContext;

    private final JeroMQConnectionsManager connectionsManager = new JeroMQConnectionsManager();

    private final SyncWait<Void> threadBlocker = new SyncWait<>(logger);

    public JeroMQMultiplexedConnectionRunnable(final String controlAddress,
                                               final ZContext zContext) {
        this.controlAddress = controlAddress;
        this.zContext = zContext;
    }

    public void blockCurrentThreadUntilControlChannelIsConnected() {
        threadBlocker.get();
    }

    @Override
    public void run() {
        try (final ZContext context = shadow(zContext)) {

            connectionsManager.registerSetupHandler(connectionsManager -> {
                logger.info("Binding control socket....");
                final int controlSocketHandle = connectionsManager.bindToAddressAndBeginPolling(
                        controlAddress,
                        PULL,
                        this::handleControlMessage
                );
                logger.info("Successfully bound control socket to handle: {}.", controlSocketHandle);

                threadBlocker.getResultConsumer().accept(null);
            });

            connectionsManager.start(context);
        }
    }

    private void handleControlMessage(
            final int socketHandle,
            final ZMsg msg,
            final JeroMQConnectionsManager connectionsManager
    ) {
        logger.info("Recv control msg");
    }

    private void sendToInprocChannel(final ZMsg msg, final BackendChannelTable backendChannelTable) {
        final RoutingHeader routingHeader = RouteRepresentationUtil.getAndStripRoutingHeader(msg);

        if (routingHeader.status.get() == CONTINUE) {

            final String backendAddress = routingHeader.backendAddress.get();
            final UUID inprocIdentifier = routingHeader.inprocIdentifier.get();

            if (backendAddress == null || inprocIdentifier == null) {
                logger.warn("Bad routeRepresentationUtil header format (missing backendAddress and/or inprocIdentifier).  Dropping message.");
            }

            final ZMQ.Socket inprocSocket = backendChannelTable.getInprocSocket(backendAddress, inprocIdentifier);

            if (inprocSocket == null) {
                logger.warn("Host unreachable.  Dropping message.");
                return;
            }

            try {
                msg.send(inprocSocket);
            } catch (ZMQException ex) {
                if (ex.getErrorCode() == EHOSTUNREACH) {
                    logger.warn("Host unreachable.  Dropping message.");
                } else {
                    throw ex;
                }
            }

        } else {
            logger.error("Received {} route for inprocIdentifier {}", routingHeader.status.get(), routingHeader.inprocIdentifier.get());
        }

    }

    private void sendToBackendChannel(final ZMsg msg, final int inprocSocketHandle, final BackendChannelTable backendChannelTable) {
        final int backendSocketHandle = backendChannelTable.getBackendSocketHandleForInprocSocketHandle(inprocSocketHandle);
        final String backendAddress = backendChannelTable.getBackendAddressForInprocSocketHandle(inprocSocketHandle);
        final UUID inprocIdentifier = backendChannelTable.getInprocIdentifier(backendSocketHandle, inprocSocketHandle);

        final RoutingHeader routingHeader = new RoutingHeader();
        routingHeader.status.set(CONTINUE);
        routingHeader.backendAddress.set(backendAddress);
        routingHeader.inprocIdentifier.set(inprocIdentifier);

        RouteRepresentationUtil.insertRoutingHeader(msg, routingHeader);

        final ZMQ.Socket backendSocket = backendChannelTable.getBackendSocket(backendAddress);

        msg.send(backendSocket);

    }

    private void handleControlMessage(final ZMsg msg, final ZMQ.Socket controlSocket, final BackendChannelTable backendChannelTable) {

        final CommandPreamble preamble = new CommandPreamble();

        preamble.getByteBuffer().put(msg.pop().getData());

        switch(preamble.commandType.get()) {
            case STATUS_REQUEST:
                send(controlSocket, STATUS_RESPONSE, new StatusResponse().getByteBuffer());
                break;
            case ROUTING_COMMAND:
                final RoutingCommand command = new RoutingCommand();
                command.getByteBuffer().put(msg.pop().getData());
                backendChannelTable.process(command);
                break;
            default:
                logger.error("Unexpected command: {}", preamble.commandType.get());
        }

    }
}