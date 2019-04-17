package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.ResourceLockService;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import java.util.*;

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
public class ConnectionsManager implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionsManager.class);

    private final List<SetupHandler> setupHandlers = new LinkedList<>();

    private final Map<Integer, MonitorThread> monitorThreads = new LinkedHashMap<>();

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

                            messageHandler.accept(socketHandle, msg, this);
                        }
                        else if (didReceiveError) {
                            throw new InternalException(
                                    "Poller error on socket handle: " + poller.getSocket(socketHandle)
                            );
                        }
                    });
        }
    }

    /**
     * Creates a socket of the given type, connects it to the given ZMQ address, registers it into the poller, and
     * returns the resultant socket handle in the poller. The provided message handler will also be registered and
     * called whenever a ZMsg is received on the resultant socket.
     *
     * @param address the ZMQ address to which we wish to connect
     * @param socketType the type of ZMQ socket to open (e.g. DEALER)
     * @param messageHandler the lambda to be called whenever a msg is received on the socket
     * @param shouldMonitor whether or not to establish a ZMonitor thread (only available for UDP/TCP conns)
     * @return the socket handle for the connection.
     */
    public int connectToAddressAndBeginPolling(
            final String address,
            final int socketType,
            final MessageHandler messageHandler,
            final boolean shouldMonitor
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

        if (shouldMonitor) {
            setupAndStartMonitorThread(socketHandle);
        }

        return socketHandle;
    }

    /**
     * Creates a socket of the given type, binds it to the given ZMQ addresses, registers it into the poller, and returns
     * the resultant socket handle in the poller. The provided message handler will also be registered and called
     * whenever a ZMsg is received on the resultant socket.
     *
     * Note: if the socketType is ROUTER, then we will perform `socket.setRouterMandatory(true)`.
     *
     * @param addresses the ZMQ addresses to which we wish to connect
     * @param socketType the type of ZMQ socket to open (e.g. ROUTER, PULL)
     * @param messageHandler the lambda to be called whenever a msg is received on the socket
     * @param shouldMonitor whether or not to establish a ZMonitor thread (only available for UDP/TCP conns)
     * @return the socket handle for the connection.
     */
    public int bindToAddressesAndBeginPolling(
            final Set<String> addresses,
            final int socketType,
            final MessageHandler messageHandler,
            final boolean shouldMonitor
    ) {
        if (addresses == null || addresses.size() == 0) {
            throw new IllegalArgumentException("At least one address must be provided.");
        }

        if (messageHandler == null) {
            throw new IllegalArgumentException("A valid messageHandler must be provided.");
        }

        final ZMQ.Socket socket = zContext.createSocket(socketType);

        if (socketType == ROUTER) {
            socket.setRouterMandatory(true);
        }

        for (final String address : addresses) {
            if (address == null || address.length() == 0) {
                throw new IllegalArgumentException("A valid address must be provided.");
            }

            socket.bind(address);
        }

        final int socketHandle = poller.register(socket, POLLIN | POLLERR);

        messageHandlers.put(socketHandle, messageHandler);

        if (shouldMonitor) {
            setupAndStartMonitorThread(socketHandle);
        }

        return socketHandle;
    }


    public int bindToAddressAndBeginPolling(
            final String address,
            final int socketType,
            final MessageHandler messageHandler,
            final boolean shouldMonitor
    ) {
        final Set<String> addresses = new HashSet<>();
        addresses.add(address);

        return bindToAddressesAndBeginPolling(addresses, socketType, messageHandler, shouldMonitor);
    }

    public void closeAndDestroySocketHandle(final int socketHandle) throws Exception {
        if (poller == null) {
            throw new IllegalStateException("Poller not set, cannot close socket with handle: " + socketHandle);
        }

        if (zContext == null) {
            throw new IllegalStateException("zContext not set, cannot destroy socket with handle: " + socketHandle);
        }

        final Socket socket = poller.getSocket(socketHandle);

        if (socket == null) {
            logger.warn("No socket found for handle: {}", socketHandle);
            return;
        }

        poller.unregister(socket);

        socket.close();

        zContext.destroySocket(socket);

        messageHandlers.remove(socketHandle);
    }

    public boolean sendMsgToSocketHandle(final int socketHandle, final ZMsg msg) {
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

    private void setupAndStartMonitorThread(final int socketHandle) {
        final Socket socket = poller.getSocket(socketHandle);

        final MonitorThread monitorThread = new MonitorThread(
                getClass().getSimpleName(),
                logger,
                zContext,
                socket
        );

        monitorThreads.put(socketHandle, monitorThread);

        monitorThread.start();
    }

    @Override
    public void close() {
        for (int socketHandle : messageHandlers.keySet()) {
            try {
                closeAndDestroySocketHandle(socketHandle);
            }
            catch (Exception e) {
                logger.warn("Failed to close/destroy socket handle {}", socketHandle);
            }

        }

        for (final MonitorThread monitorThread : monitorThreads.values()) {
            monitorThread.close();
        }

        zContext = null;
        poller = null;
        messageHandlers = null;
    }

    @FunctionalInterface
    public interface SetupHandler {

        void accept(ConnectionsManager connectionsManager);


        default SetupHandler andThen(SetupHandler after) {
            Objects.requireNonNull(after);

            return (a) -> {
                accept(a);
                after.accept(a);
            };
        }
    }

    @FunctionalInterface
    public interface MessageHandler {

        void accept(int socketHandle, ZMsg msg, ConnectionsManager connectionsManager);


        default MessageHandler andThen(MessageHandler after) {
            Objects.requireNonNull(after);

            return (a, b, c) -> {
                accept(a, b, c);
                after.accept(a, b, c);
            };
        }
    }
}