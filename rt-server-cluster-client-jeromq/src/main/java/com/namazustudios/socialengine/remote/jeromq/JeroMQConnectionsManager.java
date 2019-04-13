package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;
import sun.plugin2.message.Message;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    private final List<SetupHandler> setupHandlers = new LinkedList<>();

    private ZContext zContext;

    private Map<Integer, MessageHandler> messageHandlers;

    private Poller poller;


    /**
     * Registers a lambda method to be called during the setup process, after initializing the poller but before
     * entering the thread-blocking poll loop. The lambda will be called from within the same thread as the poll loop,
     * and should be used to issue initial commands such as manually establishing the first connections before control
     * is handed over to any control sockets. Calling this method should be thread-safe, so long as the caller recognizes
     * that the lambda will necessarily be called within the poll loop thread.
     *
     * @param setupHandler a method to be invoked on the connection poll thread.
     */
    public void registerSetupHandler(final SetupHandler setupHandler) {
        setupHandlers.add(setupHandler);
    }

    public void start(final ZContext zContext) {
        this.zContext = shadow(zContext);

        setupAndStartPollerOnCurrentThread();
    }

    private void setupAndStartPollerOnCurrentThread() {
        poller = zContext.createPoller(0);

        messageHandlers = new LinkedHashMap<>();

        for (SetupHandler setupHandler : setupHandlers) {
            setupHandler.accept(this);
        }

        enterPollLoop();
        onPollLoopExit();
    }

    private void enterPollLoop() {
        while (!interrupted()) {
            if (poller.poll(5000) < 0) {
                logger.info("Interrupted. Exiting gracefully.");
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
                                logger.warn("Message Handler not found for socket handle {}. Dropping message.", socketHandle);
                                return;
                            }

                            final MessageHandler messageHandler = messageHandlers.get(socketHandle);

                            messageHandler.accept(msg, socketHandle, this);
                        }
                        else if (didReceiveError) {
                            throw new InternalException(
                                    "Poller error on socket handle: " + poller.getSocket(socketHandle)
                            );
                        }
                    });
        }
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
     * @param socketType the type of ZMQ socket to open (e.g. DEALER)
     * @param messageHandler the lambda to be called whenever a msg is received on the socket
     * @return the socket handle for the connection.
     */
    public int connectToAddressAndBeginPolling(
            final String address,
            final int socketType,
            final MessageHandler messageHandler
    ) {
        if (address == null || address.length() == 0) {
            throw new IllegalArgumentException("A valid address must be provided.");
        }
        if (messageHandler == null) {
            throw new IllegalArgumentException("A valid messageHandler must be provided.");
        }

        final ZMQ.Socket socket = zContext.createSocket(socketType);
        socket.connect(address);

        final int socketHandle = poller.register(socket, POLLIN | POLLERR);

        messageHandlers.put(socketHandle, messageHandler);

        return socketHandle;
    }

    /**
     * Creates a ROUTER socket, binds it to the given ZMQ address, registers it into the poller, and returns the
     * resultant socket handle in the poller. The provided message handler will also be registered and called whenever
     * a ZMsg is received on the resultant socket.
     *
     * @param address the ZMQ address to which we wish to connect
     * @param socketType the type of ZMQ socket to open (e.g. ROUTER, PULL)
     * @param messageHandler the lambda to be called whenever a msg is received on the socket
     * @return the socket handle for the connection.
     */
    public int bindToAddressAndBeginPolling(
            final String address,
            final int socketType,
            final MessageHandler messageHandler
    ) {
        if (address == null || address.length() == 0) {
            throw new IllegalArgumentException("A valid address must be provided.");
        }
        if (messageHandler == null) {
            throw new IllegalArgumentException("A valid messageHandler must be provided.");
        }

        final ZMQ.Socket socket = zContext.createSocket(socketType);

        if (socketType == ROUTER) {
            socket.setRouterMandatory(true);
        }

        socket.bind(address);

        final int socketHandle = poller.register(socket, POLLIN | POLLERR);

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

    @FunctionalInterface
    public interface SetupHandler {

        void accept(JeroMQConnectionsManager connectionsManager);


        default SetupHandler andThen(SetupHandler after) {
            Objects.requireNonNull(after);

            return (c) -> {
                accept(c);
                after.accept(c);
            };
        }
    }

    @FunctionalInterface
    public interface MessageHandler {

        void accept(ZMsg zmsg, int socketHandle, JeroMQConnectionsManager connectionsManager);


        default MessageHandler andThen(MessageHandler after) {
            Objects.requireNonNull(after);

            return (z, s, c) -> {
                accept(z, s, c);
                after.accept(z, s, c);
            };
        }
    }
}