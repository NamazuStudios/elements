package com.namazustudios.socialengine.rt.jeromq;

/**
 * Represents a specific type of {@link AsyncConnection} which may be pooled.  The {@link #recycle()} is used to return
 * the {@link AsyncConnection} to a pool where it can be re-used later.
 */
public interface PooledAsyncConnection extends AsyncConnection<PooledAsyncConnection> {

    /**
     * Returns this {@link PooledAsyncConnection} to the pool where it may be reused by another process.
     */
    void recycle();

}
