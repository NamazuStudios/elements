    package com.namazustudios.socialengine.rt;

import java.util.function.Consumer;

/**
 * An interface to an underlying pool of sockets.
 */
public interface AsyncConnectionPool<ContextT, SocketT> extends AutoCloseable {

    /**
     * Acquires a new {@link AsyncConnection}.  Once assigned to a thread, the supplied {@link Consumer} will be
     * called by the IO Thread with an allocated instance of {@link AsyncConnection<ContextT, SocketT>}.  This method
     * will block until an {@link AsyncConnection} is available.
     *
     * @param asyncConnectionConsumer the {@link Consumer} which will accept
     *                                the {@link AsyncConnection<ContextT, SocketT>}
     */
    void acquireNextAvailableConnection(Consumer<AsyncConnection<ContextT, SocketT>> asyncConnectionConsumer);

    /**
     * Closes this {@link  AsyncConnectionPool<ContextT, SocketT>} and releases
     * all {@link AsyncConnection<ContextT, SocketT>} resources stored therein.  Safe to call from any thread.
     */
    void close();

}
