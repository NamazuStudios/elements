package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.Subscription;
import org.zeromq.ZMQ;

import java.util.function.Consumer;

/**
 * Represents an asynchornous type of {@link Connection}.  Note that instances of {@link PooledAsyncConnection} aren't
 * thread-safe.  Therefore you must ensure that all manipulation of the {@link PooledAsyncConnection} instance is
 * performed within a callback from the subscribed events.
 */
public interface AsyncConnection<ConnectionT extends AsyncConnection<ConnectionT>> extends Connection {

    /**
     * Registers a {@link Subscription} for when a {@link ZMQ.Socket} has read data.
     *
     * @return a {@link Subscription} to the event.
     */
    Subscription onRead(Consumer<ConnectionT> asyncConnectionConsumer);

    /**
     * Registers a {@link Subscription} for when a {@link ZMQ.Socket} is ready to write data.
     *
     * @return a {@link Subscription} to the event.
     */
    Subscription onWrite(Consumer<ConnectionT> asyncConnectionConsumer);

    /**
     * Registers a {@link Subscription} for when a {@link ZMQ.Socket} has encountered an error.
     *
     * @return a {@link Subscription} to the event.
     */
    Subscription onError(Consumer<ConnectionT> asyncConnectionConsumer);

}
