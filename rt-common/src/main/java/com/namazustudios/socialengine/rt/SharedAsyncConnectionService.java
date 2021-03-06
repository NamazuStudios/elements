package com.namazustudios.socialengine.rt;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * A type of {@link AsyncConnectionService} which uses an internal reference count to ensure that the backed
 * {@link AsyncConnectionService} is opened and closed properly.
 *
 * This is mainly intended for testing as production level code should share a single global-instance of
 * {@link AsyncConnectionService}
 *
 * @param <ContextT>
 * @param <SocketT>
 */
public class SharedAsyncConnectionService<ContextT, SocketT> implements AsyncConnectionService<ContextT, SocketT> {

    private final AtomicInteger count = new AtomicInteger();

    private final AsyncConnectionService<ContextT, SocketT> delegate;

    @Inject
    public SharedAsyncConnectionService(final AsyncConnectionService<ContextT, SocketT> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void start() {
        if (count.getAndIncrement() == 0) delegate.start();
    }

    @Override
    public void stop() {
        if (count.decrementAndGet() == 0) delegate.stop();
    }

    @Override
    public AsyncConnectionGroup.Builder<ContextT, SocketT> group(final String name) {
        return delegate.group(name);
    }

    @Override
    public AsyncConnectionPool<ContextT, SocketT> allocatePool(
            final String name,
            final int minConnections, final int maxConnections,
            final Function<ContextT, SocketT> socketSupplier) {
        return delegate.allocatePool(name, minConnections, maxConnections, socketSupplier);
    }

}
