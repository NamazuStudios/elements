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
import static java.lang.Math.min;
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

    private final Set<JeroMQAsyncConnection> connections = newKeySet();

    private final Queue<JeroMQAsyncConnection> available = new ConcurrentLinkedQueue<>();

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

        parentContext.getThreadContextRoundRobin().forEach(c -> c.doInThread(() -> {
            c.onPostLoop((s, v) -> ensureMinimum(s, c));
        }));

    }

    private void ensureMinimum(final Subscription subscription, final JeroMQAsyncThreadContext context) {
        if (open.get()) {

            int added = 0;
            final int toAdd = min((min - connections.size()), max) / THREAD_POOL_SIZE;

            while (connections.size() < max && connections.size() < min && (added++ < toAdd)) {
                final JeroMQAsyncConnection connection = context.allocateNewConnection(socketSupplier);
                addConnection(connection);
                available.add(connection);
            }

            if (connections.size() > max) {
                logger.warn("Exceeded connection pool size of {} (actual {})", connections.size(), max);
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

        final JeroMQAsyncConnection connection = available.poll();

        if (connection == null) {
            doAcqureNew(asyncConnectionConsumer);
        } else {
            connection.signal(asyncConnectionConsumer);
        }

    }

    private void doAcqureNew(final Consumer<AsyncConnection<ZContext, ZMQ.Socket>> asyncConnectionConsumer) {

        final JeroMQAsyncThreadContext context = this.context.getThreadContextRoundRobin().getNext();

        context.doInThread(() -> {
            final JeroMQAsyncConnection connection = context.allocateNewConnection(socketSupplier);
            addConnection(connection);
            asyncConnectionConsumer.accept(connection);
        });

    }

    private void addConnection(final JeroMQAsyncConnection connection) {

        connections.add(connection);

        connection.onClose(c -> {
            if (available.remove(connection)) logger.warn("Should not removed available connection {}", connection);
            if (!connections.remove(connection)) logger.warn("Could not remove connection {}", connection);
            semaphore.release();
        });

        connection.onRecycle(c0 -> {
            connection.clearEvents();
            connection.getOnError().clear();
            connection.getOnRead().clear();
            connection.getOnWrite().clear();
            available.add(connection);
            semaphore.release();
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

        connections.stream().collect(toList()).forEach(c -> c.signal(z -> c.close()));
        connections.clear();

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
