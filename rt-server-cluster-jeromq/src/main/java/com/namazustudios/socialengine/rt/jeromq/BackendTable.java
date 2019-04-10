package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.exception.MultiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMQ.Poller.POLLERR;

public class BackendTable implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(RoutingTable.class);

    private final Map<Integer, String> backendAddresses = new LinkedHashMap<>();

    private final Map<String, Integer> reverseBackendAddresses = new LinkedHashMap<>();

    // one routing table per backend
    private final Map<String, RoutingTable> backendRoutingTables = new LinkedHashMap<>();

    private final ZContext zContext;

    private final ZMQ.Poller poller;

    private final Function<String, ZMQ.Socket> backendConnector;

    private final Function<UUID, ZMQ.Socket> inprocConnector;

    public BackendTable(
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

        for (RoutingTable backendRoutingTable : backendRoutingTables.values()) {
            backendRoutingTable.close();
        }

        backendRoutingTables.clear();

        final List<Exception> exceptionList = backendAddresses.keySet().stream().map(backendAddressIndex -> {

            final ZMQ.Socket backendSocket = getPoller().getSocket(backendAddressIndex);

            if (backendSocket == null) {
                logger.warn("No backend socket at index {}", backendAddressIndex);
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

        backendAddresses.clear();
        reverseBackendAddresses.clear();

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }

    }

    public void close(final int backendAddressIndex) {

        final String backendAddress = backendAddresses.remove(backendAddressIndex);

        if (backendAddress != null &&
                reverseBackendAddresses.remove(backendAddress) != null) {
            backendRoutingTables.get(backendAddress).close();
            backendRoutingTables.remove(backendAddress);

            final ZMQ.Socket backendSocket = getPoller().getSocket(backendAddressIndex);
            close(backendSocket);
        }

    }

    public void close(final String backendAddress) {

        final Integer backendAddressIndex = reverseBackendAddresses.remove(backendAddress);

        if (backendAddressIndex != null && backendAddresses.remove(backendAddress) != null) {
            backendRoutingTables.get(backendAddress).close();
            backendRoutingTables.remove(backendAddress);

            final ZMQ.Socket socket = getPoller().getSocket(backendAddressIndex);
            close(socket);
        }

    }

    private void close(final ZMQ.Socket backendSocket) {
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

    public ZMQ.Socket getBackendSocket(String backendAddress) {
        final Integer backendAddressIndex = reverseBackendAddresses.get(backendAddress);

        if (backendAddressIndex == null) {
            return null;
        }

        final ZMQ.Socket backendSocket = getPoller().getSocket(backendAddressIndex);

        return backendSocket;
    }

    public String getBackendAddress(final int backendAddressIndex) {
        return backendAddresses.get(backendAddressIndex);
    }

    public boolean hasBackendAddress(final String backendAddressIndex) {
        return reverseBackendAddresses.containsKey(backendAddressIndex);
    }

    public RoutingTable getRoutingTable(final String backendAddress) {
        final RoutingTable backendRoutingTable = backendRoutingTables.get(backendAddress);

        return backendRoutingTable;
    }

    public RoutingTable getRoutingTable(final int backendAddressIndex) {
        if (!backendAddresses.containsKey(backendAddressIndex)) {
            return null;
        }

        final String backendAddress = backendAddresses.get(backendAddressIndex);

        final RoutingTable backendRoutingTable = backendRoutingTables.get(backendAddress);

        return backendRoutingTable;
    }

    public UUID getInprocIdentifier(final int backendAddressIndex, final int inprocIndex) {
        final RoutingTable backendRoutingTable = getRoutingTable(backendAddressIndex);

        if (backendRoutingTable == null) {
            return null;
        }

        final UUID inprocIdentifier = backendRoutingTable.getDestination(inprocIndex);

        return inprocIdentifier;
    }



    public ZMQ.Socket getInprocSocket(final int backendAddressIndex, final int inprocIndex) {
        final RoutingTable backendRoutingTable = getRoutingTable(backendAddressIndex);

        if (backendRoutingTable == null) {
            return null;
        }

        final UUID inprocIdentifier = getInprocIdentifier(backendAddressIndex, inprocIndex);

        if (inprocIdentifier == null) {
            return null;
        }

        final ZMQ.Socket inprocSocket = backendRoutingTable.getSocket(inprocIdentifier);

        return inprocSocket;
    }

    public int openBackend(final String backendAddress) {
        if (reverseBackendAddresses.containsKey(backendAddress)) {
            final int backendAddressIndex = reverseBackendAddresses.get(backendAddress);
            return backendAddressIndex;
        }

        final ZMQ.Socket backendSocket = backendConnector.apply(backendAddress)
        final int backendAddressIndex = getPoller().register(backendSocket, POLLIN | POLLERR);

        backendAddresses.put(backendAddressIndex, backendAddress);
        reverseBackendAddresses.put(backendAddress, backendAddressIndex);

        return backendAddressIndex;
    }

    public int openInproc(final String backendAddress, final UUID inprocIdentifier) {
        final RoutingTable backendRoutingTable = getRoutingTable(backendAddress);

        if (backendRoutingTable == null) {
            return -1;
        }

        final int inprocIndex = backendRoutingTable.open(inprocIdentifier);

        return inprocIndex;
    }

    /**
     * Processes the supplied {@link RoutingCommand} and applies changes to the internals of this {@link RoutingTable}.
     *
     * @param command the command to process.
     */
    public void process(final RoutingCommand command) {
        switch (command.action.get()) {
            case OPEN:
                open(command.destination.get());
                return;
            case CLOSE:
                close(command.destination.get());
                return;
        }
    }

}
