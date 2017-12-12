package com.namazustudios.socialengine.remote.jeromq;

import org.zeromq.ZMQ.Socket;

/**
 * Responsible for creating, configurating, and connecting instances of {@link Socket}.
 */
public interface ClientConnectionFactory {

    /**
     * Creates a {@link Socket} for the remote node.
     *
     * @return the {@link Socket} used for remote connections.
     */
    Connection connect();

    /**
     * Represents a connection to the remote node.
     */
    interface Connection extends AutoCloseable {

        /**
         * Obtains the {@link Socket} instance used to communicate with the remote node.  This must always return the
         * same instance of {@link Socket} per {@link Connection} instance
         *
         * @return the {@link Socket} instance
         */
        Socket socket();

        /**
         * Closes this {@link Connection}, which may close the underlying socket or it may just return the associated
         * {@link Socket} to the connection pool.
         */
        @Override
        void close();

    }

}
