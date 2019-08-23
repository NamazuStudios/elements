package com.namazustudios.socialengine.rt.jeromq;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a Connection pool which is meant to be used in conjunction with.
 */
public interface AsyncConnectionPool {

    String TIMEOUT = "com.namazustudios.socialengine.rt.jeromq.AsyncConnectionPool.timeout";

    String MIN_CONNECTIONS = "com.namazustudios.socialengine.rt.jeromq.AsyncConnectionPool.minConnections";

    String MAX_CONNECTIONS = "com.namazustudios.socialengine.rt.jeromq.AsyncConnectionPool.maxConnections";

    /**
     * Starts the {@link AsyncConnectionPool}, blocking as necessary to startup and connect.  This accepts a
     * {@link Function} which will supply the {@link ZMQ.Socket} instances.  Note that each {@link ZMQ.Socket} supplied
     * should be interchangeable with any other as this pool will recycle {@link ZMQ.Socket} instances as needed.
     */
    default void start(Function<ZContext, ZMQ.Socket> socketSupplier) {
        start(socketSupplier, "");
    }

    /**
     * Starts the {@link AsyncConnectionPool}, blocking as necessary to startup and connect.  This accepts a
     * {@link Function} which will supply the {@link ZMQ.Socket} instances.  Note that each {@link ZMQ.Socket} supplied
     * should be interchangeable with any other as this pool will recycle {@link ZMQ.Socket} instances as needed.
     */
    void start(final Function<ZContext, ZMQ.Socket> socketSupplier, final String name);

    /**
     * Stops the {@link ConnectionPool}, blocking as necessary to stop all threads as well as close and destroy all
     * sockets in the pool.
     */
    void stop();

    /**
     * Gets an instance of {@link AsyncConnection}, blocking until one becomes available.  The supplied
     * {@link Consumer<AsyncConnection>} will be called on a background thread which will be used to process the
     * connection.  For the lifecycle of the {@link AsyncConnection}, the same thread will be used to execute all
     * callbacks.
     */
    void acquireNextAvailableConnection(final Consumer<AsyncConnection> asyncConnectionConsumer);

}
