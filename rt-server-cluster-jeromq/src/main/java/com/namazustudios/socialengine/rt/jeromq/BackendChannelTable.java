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

    private final ZContext zContext;

    private final ZMQ.Poller poller;

    private final Function<String, ZMQ.Socket> backendConnector;

    private final Function<UUID, ZMQ.Socket> inprocConnector;

    private final Set<Integer> inprocSocketHandles = new LinkedHashSet<>();

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

        final List<Exception> exceptionList = backendSocketHandlesToAddresses.keySet().stream().map(backendAddressSocketHandle -> {

            final ZMQ.Socket backendSocket = getPoller().getSocket(backendAddressSocketHandle);

            if (backendSocket == null) {
                logger.warn("No backend socket for handle {}", backendAddressSocketHandle);
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

        inprocSocketHandles.clear();

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }

    }

    public void closeBackendChannel(final int backendAddressSocketHandle) {

        final String backendAddress = backendSocketHandlesToAddresses.remove(backendAddressSocketHandle);

        if (backendAddress != null &&
                backendAddressesToSocketHandles.remove(backendAddress) != null) {
            backendAddressesToInprocChannelTables.get(backendAddress).close();
            backendAddressesToInprocChannelTables.remove(backendAddress);

            final ZMQ.Socket backendSocket = getPoller().getSocket(backendAddressSocketHandle);
            closeBackendSocket(backendSocket);
        }

    }

    public void closeBackendChannel(final String backendAddress) {

        final Integer backendAddressSocketHandle = backendAddressesToSocketHandles.remove(backendAddress);

        if (backendAddressSocketHandle != null && backendSocketHandlesToAddresses.remove(backendAddress) != null) {
            backendAddressesToInprocChannelTables.get(backendAddress).close();
            backendAddressesToInprocChannelTables.remove(backendAddress);

            final ZMQ.Socket socket = getPoller().getSocket(backendAddressSocketHandle);
            closeBackendSocket(socket);
        }

    }

    private void closeBackendSocket(final ZMQ.Socket backendSocket) {
        if (backendSocket != null) {

            getPoller().unregister(backendSocket);

            try {
                backendSocket.close();
            } catch (Exception ex) {
                logger.error("Unable to close socket.", ex);
            } finally {
                getzContext().destroySocket(backendSocket);
            }

        }
    }

    public void closeInprocChannel(String backendAddress, UUID inprocIdentifier) {
        if (!backendAddressesToInprocChannelTables.containsKey(backendAddress)) {
            return;
        }

        final InprocChannelTable backendInprocChannelTable = backendAddressesToInprocChannelTables.get(backendAddress);
        final int inprocSocketHandle = backendInprocChannelTable.getInprocSocketHandle(inprocIdentifier);
        inprocSocketHandles.remove(inprocSocketHandle);

        backendInprocChannelTable.close(inprocIdentifier);
    }

    public ZMQ.Socket getBackendSocket(String backendAddress) {
        final Integer backendAddressSocketHandle = backendAddressesToSocketHandles.get(backendAddress);

        if (backendAddressSocketHandle == null) {
            return null;
        }

        final ZMQ.Socket backendSocket = getPoller().getSocket(backendAddressSocketHandle);

        return backendSocket;
    }

    public String getBackendAddress(final int backendAddressSocketHandle) {
        return backendSocketHandlesToAddresses.get(backendAddressSocketHandle);
    }

    public boolean hasBackendAddress(final String backendAddressSocketHandle) {
        return backendAddressesToSocketHandles.containsKey(backendAddressSocketHandle);
    }

    public InprocChannelTable getRoutingTable(final String backendAddress) {
        final InprocChannelTable backendInprocChannelTable = backendAddressesToInprocChannelTables.get(backendAddress);

        return backendInprocChannelTable;
    }

    public InprocChannelTable getRoutingTable(final int backendAddressSocketHandle) {
        if (!backendSocketHandlesToAddresses.containsKey(backendAddressSocketHandle)) {
            return null;
        }

        final String backendAddress = backendSocketHandlesToAddresses.get(backendAddressSocketHandle);

        final InprocChannelTable backendInprocChannelTable = backendAddressesToInprocChannelTables.get(backendAddress);

        return backendInprocChannelTable;
    }

    public UUID getInprocIdentifier(final int backendAddressSocketHandle, final int inprocSocketHandle) {
        final InprocChannelTable backendInprocChannelTable = getRoutingTable(backendAddressSocketHandle);

        if (backendInprocChannelTable == null) {
            return null;
        }

        final UUID inprocIdentifier = backendInprocChannelTable.getInprocIdentifier(inprocSocketHandle);

        return inprocIdentifier;
    }

    public ZMQ.Socket getInprocSocket(final int backendAddressSocketHandle, final int inprocSocketHandle) {
        final InprocChannelTable backendInprocChannelTable = getRoutingTable(backendAddressSocketHandle);

        if (backendInprocChannelTable == null) {
            return null;
        }

        final UUID inprocIdentifier = getInprocIdentifier(backendAddressSocketHandle, inprocSocketHandle);

        if (inprocIdentifier == null) {
            return null;
        }

        final ZMQ.Socket inprocSocket = backendInprocChannelTable.getSocket(inprocIdentifier);

        return inprocSocket;
    }

    public int openBackendChannel(final String backendAddress) {
        if (backendAddressesToSocketHandles.containsKey(backendAddress)) {
            final int backendAddressSocketHandle = backendAddressesToSocketHandles.get(backendAddress);
            return backendAddressSocketHandle;
        }

        final ZMQ.Socket backendSocket = backendConnector.apply(backendAddress);
        final int backendAddressSocketHandle = getPoller().register(backendSocket, POLLIN | POLLERR);

        backendSocketHandlesToAddresses.put(backendAddressSocketHandle, backendAddress);
        backendAddressesToSocketHandles.put(backendAddress, backendAddressSocketHandle);

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

        return backendAddressSocketHandle;
    }

    public int openInprocChannel(final String backendAddress, final UUID inprocIdentifier) {
        final InprocChannelTable backendInprocChannelTable = getRoutingTable(backendAddress);

        if (backendInprocChannelTable == null) {
            return -1;
        }

        final int inprocSocketHandle = backendInprocChannelTable.open(inprocIdentifier);

        inprocSocketHandles.add(inprocSocketHandle);

        return inprocSocketHandle;
    }

    /**
     * Processes the supplied {@link RoutingCommand} and applies changes to the internals of this {@link InprocChannelTable}.
     *
     * @param command the command to process.
     */
    public void process(final RoutingCommand command) {
        final Action action = command.action.get();
        final String backendAddress = command.backendAddress.get();
        final UUID inprocIdentifier = command.inprocIdentifier.get();

        switch (command.action.get()) {
            case OPEN_BACKEND:
                openBackendChannel(backendAddress);
                break;
            case CLOSE_BACKEND:
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
