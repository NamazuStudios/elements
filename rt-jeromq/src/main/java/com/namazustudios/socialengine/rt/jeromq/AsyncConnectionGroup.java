package com.namazustudios.socialengine.rt.jeromq;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a group of {@link AsyncConnection}s which all are serviced by the same thread.  This guarantees that
 * each {@link AsyncConnection} may call directly into another.  Closing the group will close all
 * {@link AsyncConnection} instances contained therein.  However it is not necessary to close the group explicitly.
 */
public interface AsyncConnectionGroup extends AutoCloseable {

    /**
     * Returns the number of {@link AsyncConnection} instances in this {@link AsyncConnectionGroup}.
     *
     * @return the size
     */
    int size();

    /**
     * Gets the {@link AsyncConnection} at the provided index.  The {@link AsyncConnection}s will be returned in
     * the order in which {@link Builder#connection(Function, Consumer)} was invoked.
     *
     * @param index the index of the {@link AsyncConnection}
     *
     * @return the {@link AsyncConnection}
     */
    AsyncConnection get(int index);

    /**
     * Similar to {@link AsyncConnection#signal(Consumer)}, this executes the the supplied {@link Consumer< AsyncConnectionGroup >}
     * within the thread that is servicing all of the underlying {@link AsyncConnection} instances associated
     * with the {@link AsyncConnectionGroup}
     *
     * @param consumer
     */
    void signal(Consumer<AsyncConnectionGroup> consumer);

    /**
     * Closes all {@link Connection} instances assocaited with this {@link AsyncConnectionGroup}.  Safe to call from any
     * thread.
     */
    void close();

    interface Builder {

        /**
         * Adds a {@link AsyncConnection} with the supplied creation and consumer function.
         *
         * @param socketSupplier the {@link Function< ZContext ,  ZMQ.Socket >} to supply the {@link ZMQ.Socket}
         * @param asyncConnectionConsumer the {@link Consumer<AsyncConnection>} which will be called when the connection is ready
         * @return this instance
         */
        Builder connection(Function<ZContext, ZMQ.Socket> socketSupplier,
                           Consumer<AsyncConnection> asyncConnectionConsumer);

        /**
         * Builds the {@link AsyncConnectionGroup}.
         *
         * @return the {@link AsyncConnectionGroup}.
         */
        void build(Consumer<AsyncConnectionGroup> group);

    }

}
