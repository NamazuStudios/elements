package com.namazustudios.socialengine.rt.jeromq;

import java.util.function.Consumer;

/**
 * An interface to an underlying pool of sockets.
 */
public interface AsyncConnectionPool extends AutoCloseable {

    /**
     * Acquires a new {@link AsyncConnection}.  Once assigned to a thread, the supplied {@link Consumer} will be
     * called by the IO Thread with an allocated instance of {@link AsyncConnection}.  This method will block until
     * an {@link AsyncConnection} is available.
     *
     * @param asyncConnectionConsumer the {@link Consumer} which will accept the {@link AsyncConnection}
     */
    void acquireNextAvailableConnection(Consumer<AsyncConnection> asyncConnectionConsumer);

    /**
     * Closes this {@link AsyncConnectionPool} and releases all {@link AsyncConnection} resources stored therein.
     */
    void close();

}
