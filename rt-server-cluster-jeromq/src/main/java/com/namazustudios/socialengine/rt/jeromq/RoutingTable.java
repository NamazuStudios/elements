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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.zeromq.ZMQ.Poller.POLLIN;
import static org.zeromq.ZMQ.Poller.POLLERR;

/**
 * Tracks routing information by tracking indices in a {@link org.zeromq.ZMQ.Poller} instance an {@link UUID} instances
 * which ultimately map to node identifiers.
 *
 * This class is not thread safe.
 */
public class RoutingTable implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(RoutingTable.class);

    private final Map<Integer, UUID> indices = new LinkedHashMap<>();

    private final Map<UUID, Integer> reverse = new LinkedHashMap<>();

    private final ZContext zContext;

    private final ZMQ.Poller poller;

    private final Function<UUID, ZMQ.Socket> connector;

    /**
     * Creates a new instance of this {@link RoutingTable}.  Since adding a route is specific to the container, this
     * {@link RoutingTable} requires the user specify a {@link Function<UUID, org.zeromq.ZMQ.Socket>} which will handle
     * the details of opening a new connection.
     *
     * The supplied {@link org.zeromq.ZMQ.Poller} is not owned by this instance and will not close it when it is closed.
     *
     * @param zContext the context
     * @param poller the {@link org.zeromq.ZMQ.Poller} which will manage the underlying sockets
     * @param connector a {@link Function<UUID, ZMQ.Socket>} which opens an returns a {@link org.zeromq.ZMQ.Socket}
     */
    public RoutingTable(final ZContext zContext, final ZMQ.Poller poller, final Function<UUID, ZMQ.Socket> connector) {
        this.zContext = zContext;
        this.poller = poller;
        this.connector = connector;
    }

    /**
     * Returns the {@link ZContext} used by this {@link RoutingTable}.
     *
     * @return the {@link ZContext} instance
     */
    public ZContext getzContext() {
        return zContext;
    }

    /**
     * Returns the {@link ZMQ.Poller} used by this {@link RoutingTable}.
     *
     * @return the {@link ZMQ.Poller}
     */
    public ZMQ.Poller getPoller() {
        return poller;
    }

    /**
     * Closes this {@link RoutingTable} by closing and destroying all internally managed {@link ZMQ.Socket} instances.
     */
    @Override
    public void close() {

        final List<Exception> exceptionList = indices.keySet().stream().map(frontend -> {

            final ZMQ.Socket socket = getPoller().getSocket(frontend);

            if (socket == null) {
                logger.warn("No frontend socket at index {}", frontend);
                return null;
            }

            getPoller().unregister(socket);

            try {
                socket.close();
                return null;
            } catch (Exception ex) {
                return ex;
            } finally {
                getzContext().destroySocket(socket);
            }

        }).filter(e -> e != null).collect(Collectors.toList());

        indices.clear();
        reverse.clear();

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }

    }

    /**
     * Closes the {@link org.zeromq.ZMQ.Socket}
     *
     * @param index
     */
    public void close(final int index) {

        final UUID uuid = indices.remove(index);

        if (uuid != null && reverse.remove(uuid) != null) {

            final ZMQ.Socket socket = getPoller().getSocket(index);

            if (socket != null) {

                getPoller().unregister(socket);

                try {
                    socket.close();
                } catch (Exception ex) {
                    logger.error("Unable to close socket.", ex);
                } finally {
                    getzContext().destroySocket(socket);
                }

            }

        }

    }

    /**
     * Gets a {@link org.zeromq.ZMQ.Socket} with the supplied destination.
     *
     * @param destination the {@link UUID} destination of the associated {@link org.zeromq.ZMQ.Socket}
     *
     * @return the {@link org.zeromq.ZMQ.Socket} instance.
     */
    public ZMQ.Socket getSocket(UUID destination) {
        final Integer index = reverse.get(destination);
        return index == null ? null : getPoller().getSocket(index);
    }

    /**
     * Gets the {@link UUID} for the provided index.
     *
     * @param index the index of the connection.
     *
     * @return the {@link UUID}
     */
    public UUID getDestination(final int index) {
        return indices.get(index);
    }

    /**
     * Opens a new connection to the supplied {@link UUID} destination.  If already opened, this returns the existing
     * index.
     *
     * @param destination the {@link UUID} destination
     * @return the index of the {@link org.zeromq.ZMQ.Poller}
     */
    public int open(final UUID destination) {
        return reverse.computeIfAbsent(destination, d -> {
            final ZMQ.Socket socket = connector.apply(destination);
            final int index = getPoller().register(socket, POLLIN | POLLERR);
            indices.put(index, destination);
            return index;
        });
    }

}
