package com.namazustudios.socialengine.rt;

import java.util.function.Function;

/**
 * A system-wide service to house {@link AsyncConnection} instances.  Each instance of {@link AsyncConnection} is
 * managed internally and callbacks issued on a background thread dedicated to that as well as other {@link Connection}
 * instances.
 */
public interface AsyncConnectionService<ContextT, SocketT> {

    /**
     * Starts the {@link AsyncConnectionService<ContextT, SocketT> }.
     */
    void start();

    /**
     * Stops the {@link AsyncConnectionService<ContextT, SocketT> }.
     */
    void stop();

    /**
     * Creates an anonymous group.
     *
     * @return the {@link AsyncConnectionGroup.Builder<ContextT, SocketT>}
     */
    default AsyncConnectionGroup.Builder<ContextT, SocketT> group() {
        return group("<anonymous>");
    }

    /**
     * Returns a {@link AsyncConnectionGroup.Builder} which can be used to build an instance of
     * {@link AsyncConnectionGroup} for managing closely related {@link AsyncConnection} instances.
     *
     * @param name the name of the pool (used for logging and debugging)
     * @return the {@link AsyncConnectionGroup.Builder}
     */
    AsyncConnectionGroup.Builder<ContextT, SocketT> group(String name);

    /**
     * Allocates an instance of {@link AsyncConnectionPool}.
     *
     * @param minConnections
     * @param maxConnections
     * @param socketSupplier
     * @return
     */
    default AsyncConnectionPool<ContextT, SocketT> allocatePool(final int minConnections, final int maxConnections,
                                                                final Function<ContextT, SocketT> socketSupplier) {
        return allocatePool("<anonymous>", minConnections, maxConnections, socketSupplier);
    }

    /**
     * Allocates an instance of {@link AsyncConnectionPool}.
     *
     * @param name
     * @param minConnections
     * @param maxConnections
     * @param socketSupplier
     * @return
     */
    AsyncConnectionPool<ContextT, SocketT> allocatePool(String name, int minConnections, int maxConnections,
                                                        Function<ContextT, SocketT> socketSupplier);

}
