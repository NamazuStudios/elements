package com.namazustudios.socialengine.rt.jeromq;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Responsible for creating, configuring, and connecting instances of {@link Socket} and dispatching work against the
 * {@link Socket}.  Each {@link Socket} is wrapped in a {@link Connection} which can be used to perform network IO.
 *
 * The {@link Connection} manages the lifecycle of the {@link Socket} and provides startup and shutdown of its managed
 * connections.
 *
 */
public interface ConnectionPool {

    /**
     * Starts the {@link ConnectionPool}, blocking as necessary to startup and connect.  This accepts a {@link Function}
     * which will supply the {@link Socket} instances.  Note that each {@link Socket} supplied should be interchangeable
     * with any other as this pool will recycle {@link Socket} instances as needed.
     */
    default void start(Function<ZContext, Socket> socketSupplier) {
        start(socketSupplier, "");
    }

    /**
     * Starts the {@link ConnectionPool}, blocking as necessary to startup and connect.  This accepts a {@link Function}
     * which will supply the {@link Socket} instances.  Note that each {@link Socket} supplied should be interchangeable
     * with any other as this pool will recycle {@link Socket} instances as needed.
     */
    void start(final Function<ZContext, ZMQ.Socket> socketSupplier, final String name);

    /**
     * Stops the {@link ConnectionPool}, blocking as necessary to stop all threads as well as close and destroy all
     * sockets in the pool.
     */
    void stop();

    /**
     * Performs some work in the {@link ConnectionPool}.  The pool selects an arbitrary {@link Socket} and then
     * provides an instance of {@link Connection} through which the various operations are performed.  The supplied
     * {@link Consumer<Connection>} shall be run on its own thread and must not block the calling scope.
     *
     * @param consumer
     */
    void process(final Consumer<Connection> consumer);

    /**
     * Gets the high water mark, that is the most connections that the {@link ConnectionPool} has held at a single time.
     * This persists beyond the scope of {@link #start(Function)} and {@link #stop()}.
     *
     * @return the high water mark for the lifetime of the {@link ConnectionPool}
     */
    int getHighWaterMark();

    /**
     * Represents a connection to the remote node.
     */
    interface Connection extends AutoCloseable {

        /**
         * Returns the {@link ZContext} used by this {@link Connection}.
         *
         * @return the {@link ZContext} instance
         */
        ZContext context();

        /**
         * Obtains the {@link Socket} instance used to communicate with the remote node.  This must always return the
         * same instance of {@link Socket} per {@link Connection} instance
         *
         * @return the {@link Socket} instance
         */
        Socket socket();

        /**
         * Closes the {@link Connection} an destroys the associated underlying {@link Socket}.
         */
        @Override
        void close();

    }

}
