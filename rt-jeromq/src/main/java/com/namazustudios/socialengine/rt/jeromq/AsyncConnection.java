package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.Subscription;
import org.zeromq.ZMQ;
import zmq.socket.pubsub.Sub;

import java.util.function.Consumer;

/**
 * Represents an asynchornous type of {@link Connection}.  Note that instances of {@link PooledAsyncConnection} aren't
 * thread-safe.  Therefore you must ensure that all manipulation of the {@link PooledAsyncConnection} instance is
 * performed within a callback from the subscribed events.
 */
public interface AsyncConnection extends Connection {

    /**
     * Registers a {@link Subscription} for when a {@link ZMQ.Socket} has read data.
     *
     * @param asyncConnectionConsumer the {@link Consumer<AsyncConnection>} to receive the event
     * @return {@link Subscription}
     */
    <T extends AsyncConnection>
    Subscription onRead(Consumer<? super T> asyncConnectionConsumer);

    /**
     * Registers a {@link Subscription} for when a {@link ZMQ.Socket} is ready to write data.
     *
     * @param asyncConnectionConsumer the {@link Consumer<AsyncConnection>} to receive the event
     * @return {@link Subscription}
     */
    <T extends AsyncConnection>
    Subscription onWrite(Consumer<? super T> asyncConnectionConsumer);

    /**
     * Registers a {@link Subscription} for when a {@link ZMQ.Socket} has encountered an error.
     *
     * @param asyncConnectionConsumer the {@link Consumer<AsyncConnection>} to receive the event
     * @return {@link Subscription}
     */
    <T extends AsyncConnection>
    Subscription onError(Consumer<? super T> asyncConnectionConsumer);

    /**
     * Registers a {@link Subscription for when the underlying connection was closed.
     *
     * @param asyncConnectionConsumer the {@link Consumer<AsyncConnection>} to receive the event
     * @return {@link Subscription}
     */
    <T extends AsyncConnection>
    Subscription onClose(Consumer<? super T> asyncConnectionConsumer);

}
