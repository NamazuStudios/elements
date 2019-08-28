package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.AsyncConnection;
import com.namazustudios.socialengine.rt.AsyncConnectionPool;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.namazustudios.socialengine.rt.jeromq.JeroMQAsyncConnectionService.THREAD_POOL_SIZE;
import static java.util.concurrent.ConcurrentHashMap.newKeySet;
import static java.util.stream.Collectors.toList;

class JeroMQAsyncConnectionPool implements AsyncConnectionPool<ZContext, ZMQ.Socket> {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQAsyncConnectionPool.class);

    private final int min;

    private final int max;

    private final String name;

    private final Semaphore semaphore;

    private final Function<ZContext, ZMQ.Socket> socketSupplier;

    private final JeroMQAsyncConnectionService.SimpleAsyncConnectionServiceContext context;

    private final AtomicBoolean open = new AtomicBoolean(true);

    private final Set<JeroMQAsyncConnectionHandle> connectionHandles = newKeySet();

    private final Queue<JeroMQAsyncConnectionHandle> available = new ConcurrentLinkedQueue<>();

    public JeroMQAsyncConnectionPool(final String name, final int min, final int max,
                                     final Function<ZContext, ZMQ.Socket> socketSupplier,
                                     final JeroMQAsyncConnectionService.SimpleAsyncConnectionServiceContext parentContext) {
        if (min >= max) throw new IllegalArgumentException("min must be < max");
        this.min = min;
        this.max = max;
        this.name = name;
        this.socketSupplier = socketSupplier;
        this.context = parentContext;
        this.semaphore = new Semaphore(max);
        parentContext.getThreadContextRoundRobin().forEach(c -> c.onPostLoop((s, v) -> ensureMinimum(s, c)));
    }

    private void ensureMinimum(final Subscription subscription, final JeroMQAsyncThreadContext context) {
        if (open.get()) {

            int added = 0;

            while (connectionHandles.size() < min && (added++ < (min / THREAD_POOL_SIZE))) {
                final JeroMQAsyncConnectionHandle handle = context.allocateNewConnection(socketSupplier);
                final JeroMQAsyncConnection connection = context.getConnection(handle.index);
                addConnection(handle, connection);
                semaphore.release();
            }

            if (connectionHandles.size() > max) {
                logger.warn("Exceeded connection pool size of {} (actual {})", connectionHandles.size(), max);
            }

        } else {
            subscription.unsubscribe();
        }
    }

    @Override
    public void acquireNextAvailableConnection(final Consumer<AsyncConnection<ZContext, ZMQ.Socket>> asyncConnectionConsumer) {

        if (!open.get()) throw new IllegalStateException("Pool is closed.");

        try {
            semaphore.acquire();
        } catch (InterruptedException ex) {
            throw new InternalException(ex);
        }

        final JeroMQAsyncConnectionHandle entry = available.poll();

        if (entry == null) {
            doAcqureNew(asyncConnectionConsumer);
        } else {
            doReuseConnection(asyncConnectionConsumer, entry);
        }

    }

    private void doAcqureNew(final Consumer<AsyncConnection<ZContext, ZMQ.Socket>> asyncConnectionConsumer) {

        final JeroMQAsyncThreadContext context = this.context.getThreadContextRoundRobin().getNext();

        context.doInThread(() -> {
            final JeroMQAsyncConnectionHandle handle = context.allocateNewConnection(socketSupplier);
            final JeroMQAsyncConnection connection = context.getConnection(handle.index);
            addConnection(handle, connection);
            asyncConnectionConsumer.accept(connection);
        });

    }

    private void addConnection(final JeroMQAsyncConnectionHandle handle, final JeroMQAsyncConnection connection) {

        connectionHandles.add(handle);

        connection.onClose(c -> {
            connectionHandles.remove(handle);
            semaphore.release();
        });

        connection.onRecycle(c -> {
            connection.getOnError().clear();
            connection.getOnRead().clear();
            connection.getOnWrite().clear();
            available.add(handle);
            semaphore.release();
        });

    }

    private void doReuseConnection(final Consumer<AsyncConnection<ZContext, ZMQ.Socket>> asyncConnectionConsumer,
                                   final JeroMQAsyncConnectionHandle handle) {
        handle.context.doInThread(() -> {
            final AsyncConnection connection = handle.context.getConnection(handle.index);
            asyncConnectionConsumer.accept(connection);
        });
    }

    @Override
    public void close() {
        if (!open.compareAndSet(true, false)) throw new IllegalStateException("Pool is closed.");
        context.remove(this);
        doClose();
    }

    public void doClose() {

        try {
            semaphore.acquire(max);
        } catch (InterruptedException e) {
            logger.error("Could not acquire all remaining connections.", e);
        }

        connectionHandles.stream().collect(toList()).forEach(ch -> ch.getContext().doInThread(() -> {
            final AsyncConnection connection = ch.context.getConnection(ch.index);
            connection.close();
        }));

        connectionHandles.clear();

    }

    @Override
    public String toString() {
        return "SimpleManagedPool{" +
                "min=" + min +
                ", max=" + max +
                ", name='" + name + '\'' +
                ", socketSupplier=" + socketSupplier +
                ", context=" + context +
                ", open=" + open +
                '}';
    }

}
