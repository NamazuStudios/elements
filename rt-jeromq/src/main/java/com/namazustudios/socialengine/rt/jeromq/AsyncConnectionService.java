package com.namazustudios.socialengine.rt.jeromq;

import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A system-wide service to house {@link AsyncConnection} instances.  Each instance of {@link AsyncConnection} is
 * managed internally and callbacks issued on a background thread dedicated to that as well as other {@link Connection}
 * instances.
 */
public interface AsyncConnectionService {

    /**
     * Starts the {@link AsyncConnectionService}.
     */
    void start();

    /**
     * Stops the {@link AsyncConnectionService}.
     */
    void stop();

    /**
     * Opens an {@link AsyncConnection} instance.  Once assigned a {@link Thread}, the given
     * {@link Function<ZContext,  Socket>} will be called along with the {@link Consumer< AsyncConnection >}
     * allowing the caller to begin making async network calls.
     *
     * The {@link AsyncConnection} must be explictly closed later using {@link AsyncConnection#close()}.
     *
     * @param socketSupplier the {@link Function<ZContext,  Socket>} which will supply the {@link Socket}
     * @param asyncConnectionConsumer the {@link Consumer< AsyncConnection >} which will receive the socket events.
     */
    void open(Function<ZContext, Socket> socketSupplier, Consumer<AsyncConnection> asyncConnectionConsumer);

    /**
     * Allocates an instance of {@link Pool}.
     *
     * @param name
     * @param minConnections
     * @param maxConnextions
     * @param socketSupplier
     * @return
     */
    Pool allocatePool(String name,
                      int minConnections, int maxConnextions,
                      Function<ZContext, Socket> socketSupplier);

    /**
     * An interface to an underlying pool of sockets.
     */
    interface Pool extends AutoCloseable {

        /**
         * Acquires a new {@link AsyncConnection}.  Once assigned to a thread, the supplied {@link Consumer} will be
         * called by the IO Thread with an allocated instance of {@link AsyncConnection}.  This method will block until
         * an {@link AsyncConnection} is available.
         *
         * @param asyncConnectionConsumer the {@link Consumer} which will accept the {@link AsyncConnection}
         */
        void acquireNextAvailableConnection(Consumer<AsyncConnection> asyncConnectionConsumer);

        /**
         * Closes this {@link Pool} and releases all {@link AsyncConnection} resources stored therein.
         */
        void close();

    }

}
