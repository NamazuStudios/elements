package com.namazustudios.socialengine.rt.jeromq;

import static com.namazustudios.socialengine.rt.jeromq.RoutingCommand.Action;

import com.namazustudios.socialengine.rt.exception.MultiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMQ.Poller.POLLERR;

public class BackendChannelTable implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(InprocChannelTable.class);

    private final Map<Integer, String> backendSocketHandlesToAddresses = new LinkedHashMap<>();

    private final Map<String, Integer> backendAddressesToSocketHandles = new LinkedHashMap<>();

    private final Map<String, InprocChannelTable> backendAddressesToInprocChannelTables = new LinkedHashMap<>();

    private final Map<String, MonitorThread> backendAddressesToMonitorThreads = new LinkedHashMap<>();

    private final Map<Integer, Integer> inprocSocketHandlesToBackendSocketHandles = new LinkedHashMap<>();

    private final Map<Integer, String> inprocSocketHandlesToBackendAddresses = new LinkedHashMap<>();

    private final ZContext zContext;

    private final ZMQ.Poller poller;

    private final Function<String, ZMQ.Socket> backendConnector;

    private final Function<UUID, ZMQ.Socket> inprocConnector;

    public BackendChannelTable(
            final ZContext zContext,
            final ZMQ.Poller poller,
            final Function<String, ZMQ.Socket> backendConnector,
            final Function<UUID, ZMQ.Socket> inprocConnector
    ) {
        this.zContext = zContext;
        this.poller = poller;
        this.backendConnector = backendConnector;
        this.inprocConnector = inprocConnector;
    }

    public ZContext getzContext() {
        return zContext;
    }

    public ZMQ.Poller getPoller() {
        return poller;
    }

    @Override
    public void close() {

        backendAddressesToInprocChannelTables.values().forEach(backendRoutingTable -> backendRoutingTable.close());
        backendAddressesToInprocChannelTables.clear();

        backendAddressesToMonitorThreads.values().forEach(backendMonitorThread -> backendMonitorThread.close());
        backendAddressesToMonitorThreads.clear();

        final List<Exception> exceptionList = backendSocketHandlesToAddresses.keySet().stream().map(backendSocketHandle -> {

            final ZMQ.Socket backendSocket = getPoller().getSocket(backendSocketHandle);

            if (backendSocket == null) {
                logger.warn("No backend socket for handle {}", backendSocketHandle);
                return null;
            }

            getPoller().unregister(backendSocket);

            try {
                backendSocket.close();
                return null;
            } catch (Exception ex) {
                return ex;
            } finally {
                getzContext().destroySocket(backendSocket);
            }

        }).filter(e -> e != null).collect(Collectors.toList());

        backendSocketHandlesToAddresses.clear();
        backendAddressesToSocketHandles.clear();

        inprocSocketHandlesToBackendSocketHandles.clear();
        inprocSocketHandlesToBackendAddresses.clear();

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }

    }

    public void closeBackendChannel(final int backendSocketHandle) {

        final String backendAddress = backendSocketHandlesToAddresses.remove(backendSocketHandle);

        if (backendAddress != null &&
                backendAddressesToSocketHandles.remove(backendAddress) != null) {
            backendAddressesToInprocChannelTables.get(backendAddress).close();
            backendAddressesToInprocChannelTables.remove(backendAddress);

            final ZMQ.Socket backendSocket = getPoller().getSocket(backendSocketHandle);
            closeBackendSocket(backendSocket);
        }

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

    private void closeBackendSocket(final ZMQ.Socket backendSocket) {
        if (backendSocket != null) {

            getPoller().unregister(backendSocket);

            try {
                backendSocket.close();
            } catch (Exception ex) {
                logger.error("Unable to issueCloseInprocChannelCommand socket.", ex);
            } finally {
                getzContext().destroySocket(backendSocket);
            }

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

    public ZMQ.Socket getBackendSocket(String backendAddress) {
        final Integer backendSocketHandle = backendAddressesToSocketHandles.get(backendAddress);

        if (backendSocketHandle == null) {
            return null;
        }

        final ZMQ.Socket backendSocket = getPoller().getSocket(backendSocketHandle);

        return backendSocket;
    }

    public int getBackendSocketHandleForBackendAddress(final String backendAddress) {
        if (!backendAddressesToSocketHandles.containsKey(backendAddress)) {
            return -1;
        }

        return backendAddressesToSocketHandles.get(backendAddress);
    }

    public String getBackendAddressForBackendSocketHandle(final int backendSocketHandle) {
        return backendSocketHandlesToAddresses.get(backendSocketHandle);
    }

    public String getBackendAddressForInprocSocketHandle(final int inprocSocketHandle) {
        return inprocSocketHandlesToBackendAddresses.get(inprocSocketHandle);
    }

    public int getBackendSocketHandleForInprocSocketHandle(final int inprocSocketHandle) {
        if (!inprocSocketHandlesToBackendSocketHandles.containsKey(inprocSocketHandle)) {
            return -1;
        }

        return inprocSocketHandlesToBackendSocketHandles.get(inprocSocketHandle);
    }

    public boolean hasBackendAddress(final String backendAddress) {
        return backendAddressesToSocketHandles.containsKey(backendAddress);
    }

    public boolean hasBackendSocketHandle(final int backendSocketHandle) {
        return backendSocketHandlesToAddresses.containsKey(backendSocketHandle);
    }

    public boolean hasInprocSocketHandle(final int inprocSocketHandle) {
        return inprocSocketHandlesToBackendSocketHandles.containsKey(inprocSocketHandle);
    }


    public InprocChannelTable getInprocChannelTable(final String backendAddress) {
        final InprocChannelTable inprocChannelTable = backendAddressesToInprocChannelTables.get(backendAddress);

        return inprocChannelTable;
    }

    public InprocChannelTable getInprocChannelTable(final int backendSocketHandle) {
        if (!backendSocketHandlesToAddresses.containsKey(backendSocketHandle)) {
            return null;
        }

        final String backendAddress = backendSocketHandlesToAddresses.get(backendSocketHandle);

        final InprocChannelTable inprocChannelTable = backendAddressesToInprocChannelTables.get(backendAddress);

        return inprocChannelTable;
    }

    public UUID getInprocIdentifier(final int backendSocketHandle, final int inprocSocketHandle) {
        final InprocChannelTable backendInprocChannelTable = getInprocChannelTable(backendSocketHandle);

        if (backendInprocChannelTable == null) {
            return null;
        }

        final UUID inprocIdentifier = backendInprocChannelTable.getInprocIdentifier(inprocSocketHandle);

        return inprocIdentifier;
    }

    public ZMQ.Socket getInprocSocket(final String backendAddress, final UUID inprocIdentifier) {
        final InprocChannelTable backendInprocChannelTable = getInprocChannelTable(backendAddress);

        if (backendInprocChannelTable == null) {
            return null;
        }

        final ZMQ.Socket inprocSocket = backendInprocChannelTable.getInprocSocket(inprocIdentifier);

        return inprocSocket;
    }

    public ZMQ.Socket getInprocSocket(final int backendSocketHandle, final int inprocSocketHandle) {
        final InprocChannelTable backendInprocChannelTable = getInprocChannelTable(backendSocketHandle);

        if (backendInprocChannelTable == null) {
            return null;
        }

        final UUID inprocIdentifier = getInprocIdentifier(backendSocketHandle, inprocSocketHandle);

        if (inprocIdentifier == null) {
            return null;
        }

        final ZMQ.Socket inprocSocket = backendInprocChannelTable.getInprocSocket(inprocIdentifier);

        return inprocSocket;
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

    /**
     * Processes the supplied {@link RoutingCommand} and applies changes to the internals of this {@link InprocChannelTable}.
     *
     * @param command the command to process.
     */
    public void process(final RoutingCommand command) {
        final Action action = command.action.get();
        final String backendAddress = command.tcpAddress.get();
        final UUID inprocIdentifier = command.inprocIdentifier.get();

        switch (command.action.get()) {
            case CONNECT_TCP:
                openBackendChannel(backendAddress);
                break;
            case DISCONNECT_TCP:
                closeBackendChannel(backendAddress);
                break;
            case OPEN_INPROC:
                openInprocChannel(backendAddress, inprocIdentifier);
                break;
            case CLOSE_INPROC:
                closeInprocChannel(backendAddress, inprocIdentifier);
                break;
        }
    }
}
