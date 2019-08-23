package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.Subscription;
import org.zeromq.ZMQ.Socket;

import java.util.function.Consumer;

/**
 * Represents an asynchornous type of {@link Connection}.  NOte that instances of {@link AsyncConnection} aren't
 * thread-safe.  Therefore you must ensure that all manipulation of the {@link AsyncConnection} instance is performed
 * within a callback from the subscribed events.
 */
public interface AsyncConnection extends Connection {

    /**
     * Registers a {@link Subscription} for when a {@link Socket} has read data.
     *
     * @return a {@link Subscription} to the event.
     */
    Subscription onRead(Consumer<AsyncConnection> asyncConnectionConsumer);

    /**
     * Registers a {@link Subscription} for when a {@link Socket} has completed writing data.
     *
     * @return a {@link Subscription} to the event.
     */
    Subscription onWrite(Consumer<AsyncConnection> asyncConnectionConsumer);

    /**
     * Registers a {@link Subscription} for when a {@link Socket} has encountered an error.
     *
     * @return a {@link Subscription} to the event.
     */
    Subscription onError(Consumer<AsyncConnection> asyncConnectionConsumer);

    /**
     * Returns this {@link AsyncConnection} to the pool where it may be reused by another process.
     */
    void recycle();

}
