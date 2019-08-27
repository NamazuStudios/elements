package com.namazustudios.socialengine.rt.jeromq;

import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A system-wide service to house {@link PooledAsyncConnection} instances.  Each instance of {@link PooledAsyncConnection} is
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
     * Opens an {@link PooledAsyncConnection} instance.  Once assigned a {@link Thread}, the given
     * {@link Function<ZContext,  Socket>} will be called along with the {@link Consumer< PooledAsyncConnection >}
     * allowing the caller to begin making async network calls.
     *
     * The {@link PooledAsyncConnection} must be explictly closed later using {@link PooledAsyncConnection#close()}.
     *
     * @param socketSupplier the {@link Function<ZContext,  Socket>} which will supply the {@link Socket}
     * @param asyncConnectionConsumer the {@link Consumer< PooledAsyncConnection >} which will receive the socket events.
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
         * Acquires a new {@link PooledAsyncConnection}.  Once assigned to a thread, the supplied {@link Consumer} will be
         * called by the IO Thread with an allocated instance of {@link PooledAsyncConnection}.  This method will block until
         * an {@link PooledAsyncConnection} is available.
         *
         * @param asyncConnectionConsumer the {@link Consumer} which will accept the {@link PooledAsyncConnection}
         */
        void acquireNextAvailableConnection(Consumer<PooledAsyncConnection> asyncConnectionConsumer);

        /**
         * Closes this {@link Pool} and releases all {@link PooledAsyncConnection} resources stored therein.
         */
        void close();

    }

}
