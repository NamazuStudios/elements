package com.namazustudios.socialengine.remote.jeromq;

import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZSocket;
import zmq.poll.Poller;

import java.util.function.Consumer;

/**
 * Responsible for creating, configuring, and connecting instances of {@link Socket} and dispatching work against the
 * {@link Socket}.
 */
public interface ConnectionPool {

    /**
     * Starts the {@link ConnectionPool}, blocking as necessary to startup and dispatch threads.
     */
    void start();

    /**
     * Stops the {@link ConnectionPool}, blocking as necessary to stop all threads.
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
     * Represents a connection to the remote node.
     */
    interface Connection {

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

    }

}
