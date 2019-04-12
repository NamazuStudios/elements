package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.jeromq.*;
import com.namazustudios.socialengine.rt.remote.RoutingHeader;
import com.namazustudios.socialengine.rt.util.SyncWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import javax.inject.Inject;
import java.util.*;

import static com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType.STATUS_RESPONSE;
import static com.namazustudios.socialengine.rt.jeromq.Connection.from;
import static com.namazustudios.socialengine.rt.jeromq.JeroMQSocketHost.send;
import static com.namazustudios.socialengine.rt.remote.RoutingHeader.Status.CONTINUE;
import static java.lang.Thread.interrupted;
import static java.util.stream.IntStream.range;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.*;
import static org.zeromq.ZMQ.Poller.POLLERR;
import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMsg.recvMsg;
import static zmq.ZError.EHOSTUNREACH;

/**
 * Threaded manager for a multiplexed ZMQ connection.
 */
public class JeroMQMultiplexedConnectionRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQMultiplexedConnectionRunnable.class);

    private final SyncWait<Void> connectSyncWait = new SyncWait<Void>(logger);

    private final String controlAddress;

    private volatile ZContext zContext;

    public JeroMQMultiplexedConnectionRunnable(final String controlAddress,
                                               final ZContext zContext) {
        this.controlAddress = controlAddress;
        this.zContext = zContext;
    }

    public void waitForConnect() {
        connectSyncWait.get();
    }

    @Override
    public void run() {

        try (final ZContext context = shadow(zContext);
             final ZMQ.Poller poller = context.createPoller(0);
             final BackendChannelTable backendChannelTable = new BackendChannelTable(
                     context,
                     poller,
                     backendAddress -> bind(context, backendAddress),
                     inprocIdentifier -> bind(context, inprocIdentifier)
             );
             final Connection control = from(context, c -> c.createSocket(PULL));
             ) {

            final int controlSocketHandle;

            try {
                control.socket().bind(controlAddress);
                controlSocketHandle = poller.register(control.socket(), POLLIN | POLLERR);
                connectSyncWait.getResultConsumer().accept(null);
            } catch (Exception ex) {
                connectSyncWait.getErrorConsumer().accept(ex);
                return;
            }

            while (!interrupted()) {

                if (poller.poll(5000) < 0) {
                    logger.info("Interrupted.  Exiting gracefully.");
                    break;
                }

                range(0, poller.getNext())
                        .filter(socketHandle -> poller.getItem(socketHandle) != null)
                        .forEach(socketHandle -> {
                            final boolean input = poller.pollin(socketHandle);
                            final boolean error = poller.pollerr(socketHandle);

                            if (input) {
                                final ZMQ.Socket socket = poller.getSocket(socketHandle);
                                final ZMsg msg = recvMsg(socket);

                                if (socketHandle == controlSocketHandle) {  // if we recv on the control socket
                                    handleControlMessage(msg, socket, backendChannelTable);
                                }
                                else if (backendChannelTable.hasBackendSocketHandle(socketHandle)) {    // if we recv on a backend socket
                                    sendToInprocChannel(msg, backendChannelTable);
                                }
                                else if (backendChannelTable.hasInprocSocketHandle(socketHandle)) { // if we recv on an inproc socket
                                    sendToBackendChannel(msg, socketHandle, backendChannelTable);
                                }
                                else {
                                    logger.warn("Unknown poller state.  Dropping message.");
                                }

                            } else if (error) {
                                throw new InternalException("Poller error on socket: " + poller.getSocket(socketHandle));
                            }
                });

            }

        }

    }

    private ZMQ.Socket bind(final ZContext context, final String backendAddress) {
        final ZMQ.Socket backendSocket = context.createSocket(DEALER);
        backendSocket.connect(backendAddress);
        return backendSocket;
    }

    private ZMQ.Socket bind(final ZContext context, final UUID inprocIdentifier) {
        final ZMQ.Socket inprocSocket = context.createSocket(ROUTER);
        final String bindAddress = RouteRepresentationUtil.buildMultiplexedInprocAddress(inprocIdentifier);
        inprocSocket.setRouterMandatory(true);
        inprocSocket.bind(bindAddress);
        return inprocSocket;
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

    public ZContext getzContext() {
        return zContext;
    }

    @Inject
    public void setzContext(ZContext zContext) {
        this.zContext = zContext;
    }
}