package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.Subscription;
import org.zeromq.ZMQ;

import java.util.function.Consumer;

/**
 * Represents an asynchornous type of {@link Connection}.  Note that instances of {@link AsyncConnection} aren't
 * thread-safe.  Therefore you must ensure that all manipulation of the {@link AsyncConnection} instance is
 * performed within a callback from the subscribed events.
 */
public interface AsyncConnection extends Connection {

    /**
     * Sends a signal to this {@link AsyncConnection}.  The signal will run on the {@link AsyncConnection}'s thread
     * and can be used to safely manipulate the {@link AsyncConnection} from an outside thread.
     *
     * This is the only method that may be called from any thread.
     *
     * @param asyncConnectionConsumer the {@link Consumer<AsyncConnection>} to receive the event
     */
    void signal(Consumer<AsyncConnection> asyncConnectionConsumer);

    /**
     * Returns this {@link AsyncConnection}.  This hints to the underlying {@link AsyncConnectionService} that the
     * client code is done using the connection.  Depending on circumstances, this may simply close the connection.
     */
    void recycle();

    /**
     * Registers a {@link Subscription} for when a {@link ZMQ.Socket} has read data.
     *
     * @param asyncConnectionConsumer the {@link Consumer<AsyncConnection>} to receive the event
     * @return {@link Subscription}
     */
    
    Subscription onRead(Consumer<AsyncConnection> asyncConnectionConsumer);

    /**
     * Registers a {@link Subscription} for when a {@link ZMQ.Socket} is ready to write data.
     *
     * @param asyncConnectionConsumer the {@link Consumer<AsyncConnection>} to receive the event
     * @return {@link Subscription}
     */
    
    Subscription onWrite(Consumer<AsyncConnection> asyncConnectionConsumer);

    /**
     * Registers a {@link Subscription} for when a {@link ZMQ.Socket} has encountered an error.
     *
     * @param asyncConnectionConsumer the {@link Consumer<AsyncConnection>} to receive the event
     * @return {@link Subscription}
     */
    
    Subscription onError(Consumer<AsyncConnection> asyncConnectionConsumer);

    /**
     * Registers a {@link Subscription for when the underlying connection was closed.
     *
     * @param asyncConnectionConsumer the {@link Consumer<AsyncConnection>} to receive the event
     * @return {@link Subscription}
     */
    
    Subscription onClose(Consumer<AsyncConnection> asyncConnectionConsumer);

    /**
     * Subscribes to an event indicating that the connection was recycled.
     *
     * @param pooledAsyncConnectionConsumer the {@link Consumer<AsyncConnection>} to receive the event.
     *
     * @return a {@link Subscription} to the event
     */
    Subscription onRecycle(Consumer<AsyncConnection> pooledAsyncConnectionConsumer);

}
