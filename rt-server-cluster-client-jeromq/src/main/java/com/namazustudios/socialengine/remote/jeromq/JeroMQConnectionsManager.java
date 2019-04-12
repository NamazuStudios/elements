package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.jeromq.*;
import com.namazustudios.socialengine.rt.remote.RoutingHeader;
import com.namazustudios.socialengine.rt.util.SyncWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import java.util.*;
import java.util.function.BiConsumer;

import static java.lang.Thread.interrupted;
import static java.util.stream.IntStream.range;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.*;
import static org.zeromq.ZMQ.Poller.*;
import static org.zeromq.ZMsg.recvMsg;
import static zmq.ZError.EHOSTUNREACH;

/**
 * Encapsulates the ZMQ Poller and all socket operations (open, close, send, recv). This is not thread-safe and is
 * meant to be accessed from within the multiplexed connection runnable thread.
 */
public class JeroMQConnectionsManager {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQConnectionsManager.class);

    private final SyncWait<Void> connectSyncWait = new SyncWait<Void>(logger);

    private ZContext zContext;

    private Map<Integer, BiConsumer<ZMsg, JeroMQConnectionsManager>> messageHandlers;

    private Poller poller;


    public void start(final ZContext zContext) {
        this.zContext = shadow(zContext);

        setupAndStartPollerOnCurrentThread();
    }

    private void setupAndStartPollerOnCurrentThread() {
        poller = zContext.createPoller(0);

        messageHandlers = new LinkedHashMap<>();

        enterPollLoop();
    }

    private void enterPollLoop() {
        while (!interrupted()) {
            if (poller.poll(5000) < 0) {
                logger.info("Interrupted. Exiting gracefully.");
                onPollLoopExit();
                break;
            }

            range(0, poller.getNext())
                    .filter(socketHandle -> poller.getItem(socketHandle) != null)
                    .forEach(socketHandle -> {
                        final boolean didReceiveInput = poller.pollin(socketHandle);
                        final boolean didReceiveError = poller.pollerr(socketHandle);

                        if (didReceiveInput) {
                            final ZMQ.Socket socket = poller.getSocket(socketHandle);
                            final ZMsg msg = recvMsg(socket);

                            if (!messageHandlers.containsKey(socketHandle)) {
                                logger.warn(
                                        "Message Handler not found for socket handle {}. Dropping message.",
                                        socketHandle
                                );
                                return;
                            }

                            final BiConsumer<ZMsg, JeroMQConnectionsManager> messageHandler =
                                    messageHandlers.get(socketHandle);

                            messageHandler.accept(msg, this);
                        }
                        else if (didReceiveError) {
                            throw new InternalException(
                                    "Poller error on socket handle: " + poller.getSocket(socketHandle)
                            );
                        }
                    });
        }
    }

    private void handleReceivedMessage(final int socketHandle, final ZMsg msg) {

    }

    private void onPollLoopExit() {
        zContext = null;
        poller = null;
        messageHandlers = null;
    }


    /**
     * Creates a DEALER socket, connects it to the given ZMQ address, registers it into the poller, and returns the
     * resultant socket handle in the poller. The provided message handler will also be registered and called whenever
     * a ZMsg is received on the resultant socket.
     *
     * @param address the ZMQ address to which we wish to connect
     * @return the socket handle for the connection.
     */
    public int connectToAddressAndBeginPolling(
            final String address,
            final BiConsumer<ZMsg, JeroMQConnectionsManager> messageHandler
    ) {
        if (address == null || address.length() == 0) {
            throw new IllegalArgumentException("A valid address must be provided.");
        }
        if (messageHandler == null) {
            throw new IllegalArgumentException("A valid messageHandler must be provided.");
        }

        final ZMQ.Socket dealerSocket = zContext.createSocket(DEALER);
        dealerSocket.connect(address);

        final int socketHandle = poller.register(dealerSocket, POLLIN | POLLERR);

        messageHandlers.put(socketHandle, messageHandler);

        return socketHandle;
    }

    /**
     * Creates a ROUTER socket, binds it to the given ZMQ address, registers it into the poller, and returns the
     * resultant socket handle in the poller. The provided message handler will also be registered and called whenever
     * a ZMsg is received on the resultant socket.
     *
     * @param address the ZMQ address to which we wish to bind.
     * @return the socket handle for the connection.
     */
    public int bindToAddressAndBeginPolling(
            final String address,
            final BiConsumer<ZMsg, JeroMQConnectionsManager> messageHandler
    ) {
        if (address == null || address.length() == 0) {
            throw new IllegalArgumentException("A valid address must be provided.");
        }
        if (messageHandler == null) {
            throw new IllegalArgumentException("A valid messageHandler must be provided.");
        }

        final ZMQ.Socket routerSocket = zContext.createSocket(ROUTER);
        routerSocket.setRouterMandatory(true);
        routerSocket.bind(address);

        final int socketHandle = poller.register(routerSocket, POLLIN | POLLERR);

        messageHandlers.put(socketHandle, messageHandler);

        return socketHandle;
    }

    public boolean sendMsgToSocketHandle(final ZMsg msg, final int socketHandle) {
        final Socket socket = poller.getSocket(socketHandle);

        if (socket == null) {
            logger.warn("Socket with handle {} not registered in the poller. Dropping message.", socketHandle);
            return false;
        }

        try {
            msg.send(socket);
        }
        catch (ZMQException e) {
            if (e.getErrorCode() == EHOSTUNREACH) {
                logger.warn("Host unreachable. Dropping message.");
            }
            else {
                logger.warn("Unhandled exception when trying to send msg to socket handle: {}", e);
                return false;
            }
        }

        return true;
    }
}