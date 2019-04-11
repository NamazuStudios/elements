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

/**
 * Tracks routing information by tracking inprocSocketHandlesToIdentifiers in a {@link org.zeromq.ZMQ.Poller} instance an {@link UUID} instances
 * which ultimately map to node identifiers.
 *
 * This class is not thread safe.
 */
public class InprocChannelTable implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(InprocChannelTable.class);

    private final Map<Integer, UUID> inprocSocketHandlesToIdentifiers = new LinkedHashMap<>();

    private final Map<UUID, Integer> inprocIdentifiersToSocketHandles = new LinkedHashMap<>();

    private final ZContext zContext;

    private final ZMQ.Poller poller;

    private final Function<UUID, ZMQ.Socket> connector;

    /**
     * Creates a new instance of this {@link InprocChannelTable}.  Since adding a route is specific to the container, this
     * {@link InprocChannelTable} requires the user specify a {@link Function<UUID, org.zeromq.ZMQ.Socket>} which will handle
     * the details of opening a new connection.
     *
     * The supplied {@link org.zeromq.ZMQ.Poller} is not owned by this instance and will not closeInprocChannel it when it is closed.
     *
     * @param zContext the context
     * @param poller the {@link org.zeromq.ZMQ.Poller} which will manage the underlying sockets
     * @param connector a {@link Function<UUID, ZMQ.Socket>} which opens an returns a {@link org.zeromq.ZMQ.Socket}
     */
    public InprocChannelTable(final ZContext zContext, final ZMQ.Poller poller, final Function<UUID, ZMQ.Socket> connector) {
        this.zContext = zContext;
        this.poller = poller;
        this.connector = connector;
    }

    /**
     * Returns the {@link ZContext} used by this {@link InprocChannelTable}.
     *
     * @return the {@link ZContext} instance
     */
    public ZContext getzContext() {
        return zContext;
    }

    /**
     * Returns the {@link ZMQ.Poller} used by this {@link InprocChannelTable}.
     *
     * @return the {@link ZMQ.Poller}
     */
    public ZMQ.Poller getPoller() {
        return poller;
    }

    /**
     * Closes this {@link InprocChannelTable} by closing and destroying all internally managed {@link ZMQ.Socket} instances.
     */
    @Override
    public void close() {

        final List<Exception> exceptionList = inprocSocketHandlesToIdentifiers.keySet().stream().map(inprocSocketHandle -> {

            final ZMQ.Socket inprocSocket = getPoller().getSocket(inprocSocketHandle);

            if (inprocSocket == null) {
                logger.warn("No frontend socket at inprocSocketHandle {}", inprocSocketHandle);
                return null;
            }

            getPoller().unregister(inprocSocket);

            try {
                inprocSocket.close();
                return null;
            } catch (Exception ex) {
                return ex;
            } finally {
                getzContext().destroySocket(inprocSocket);
            }

        }).filter(e -> e != null).collect(Collectors.toList());

        inprocSocketHandlesToIdentifiers.clear();
        inprocIdentifiersToSocketHandles.clear();

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }

    }

    /**
     * Closes the {@link org.zeromq.ZMQ.Socket} associated with the supplied inprocSocketHandle and removes all route information
     * from the internal table.
     *
     * @param inprocSocketHandle
     */
    public void close(final int inprocSocketHandle) {

        final UUID uuid = inprocSocketHandlesToIdentifiers.remove(inprocSocketHandle);

        if (uuid != null && inprocIdentifiersToSocketHandles.remove(uuid) != null) {
            final ZMQ.Socket socket = getPoller().getSocket(inprocSocketHandle);
            close(socket);
        }

    }

    /**
     * Closes the {@link org.zeromq.ZMQ.Socket} associated with the supplied {@link UUID} and removes all information
     * from the internal table.
     *
     * @param uuid the uuid to closeInprocChannel
     */
    public void close(final UUID uuid) {

        final Integer inprocSocketHandle = inprocIdentifiersToSocketHandles.remove(uuid);

        if (inprocSocketHandle != null && inprocSocketHandlesToIdentifiers.remove(uuid) != null) {
            final ZMQ.Socket socket = getPoller().getSocket(inprocSocketHandle);
            close(socket);
        }

    }

    private void close(final ZMQ.Socket socket) {
        if (socket != null) {

            getPoller().unregister(socket);

            try {
                socket.close();
            } catch (Exception ex) {
                logger.error("Unable to closeInprocChannel socket.", ex);
            } finally {
                getzContext().destroySocket(socket);
            }

        }
    }

    /**
     * Gets a {@link org.zeromq.ZMQ.Socket} with the supplied inprocIdentifier.
     *
     * @param inprocIdentifier the {@link UUID} inprocIdentifier of the associated {@link org.zeromq.ZMQ.Socket}
     *
     * @return the {@link org.zeromq.ZMQ.Socket} instance.
     */
    public ZMQ.Socket getInprocSocket(UUID inprocIdentifier) {
        final Integer inprocSocketHandle = inprocIdentifiersToSocketHandles.get(inprocIdentifier);
        return inprocSocketHandle == null ? null : getPoller().getSocket(inprocSocketHandle);
    }

    public int getInprocSocketHandle(UUID inprocIdentifier) {
        final Integer socketHandle = inprocIdentifiersToSocketHandles.get(inprocIdentifier);
        if (socketHandle == null || socketHandle < 0) {
            return -1;
        }
        else {
            return socketHandle;
        }
    }

    /**
     * Gets the {@link UUID} for the provided inprocSocketHandle.
     *
     * @param inprocSocketHandle the socket handle.
     *
     * @return the {@link UUID}
     */
    public UUID getInprocIdentifier(final int inprocSocketHandle) {
        return inprocSocketHandlesToIdentifiers.get(inprocSocketHandle);
    }

    /**
     * Returns true if a inprocIdentifier {@link UUID} exists in this {@link InprocChannelTable}.
     *
     * @param uuid the {@link UUID}
     *
     * @return true if it exists, false otherwise
     */
    public boolean hasInprocIdentifier(final UUID uuid) {
        return inprocIdentifiersToSocketHandles.containsKey(uuid);
    }

    /**
     * Opens a new connection to the supplied {@link UUID} inprocIdentifier.  If already opened, this returns the existing
     * inprocSocketHandle.
     *
     * @param inprocIdentifier the {@link UUID} inprocIdentifier
     * @return the inprocSocketHandle of the {@link org.zeromq.ZMQ.Poller}
     */
    public int open(final UUID inprocIdentifier) {
        return inprocIdentifiersToSocketHandles.computeIfAbsent(inprocIdentifier, d -> {
            final ZMQ.Socket socket = connector.apply(inprocIdentifier);
            final int inprocSocketHandle = getPoller().register(socket, POLLIN | POLLERR);
            inprocSocketHandlesToIdentifiers.put(inprocSocketHandle, inprocIdentifier);
            return inprocSocketHandle;
        });
    }

}
