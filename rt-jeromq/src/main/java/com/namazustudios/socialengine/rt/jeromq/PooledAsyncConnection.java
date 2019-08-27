package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.Subscription;

import java.util.function.Consumer;

/**
 * Represents a specific type of {@link AsyncConnection} which may be pooled.  The {@link #recycle()} is used to return
 * the {@link AsyncConnection} to a pool where it can be re-used later.
 */
public interface PooledAsyncConnection extends AsyncConnection {

    /**
     * Subscribes to an event indicating that the connection was recycled.
     *
     * @param pooledAsyncConnectionConsumer the {@link Consumer<AsyncConnection>} to receive the event.
     *
     * @return a {@link Subscription} to the event
     */
    <T extends PooledAsyncConnection>
    Subscription onRecycle(Consumer<? super T> pooledAsyncConnectionConsumer);

    /**
     * Returns this {@link PooledAsyncConnection} to the pool where it may be reused by another process.
     */
    void recycle();

}
