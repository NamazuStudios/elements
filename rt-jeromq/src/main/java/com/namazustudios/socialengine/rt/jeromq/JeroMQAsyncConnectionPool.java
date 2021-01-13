package com.namazustudios.socialengine.rt.jeromq;

import com.google.common.collect.Streams;
import com.namazustudios.socialengine.rt.AsyncConnection;
import com.namazustudios.socialengine.rt.AsyncConnectionPool;
import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.AsynchronousCloseException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.collect.Streams.concat;
import static com.namazustudios.socialengine.rt.jeromq.JeroMQAsyncConnectionService.THREAD_POOL_SIZE;
import static java.lang.Math.*;
import static java.util.concurrent.ConcurrentHashMap.newKeySet;
import static java.util.stream.Collectors.toList;

class JeroMQAsyncConnectionPool implements AsyncConnectionPool<ZContext, ZMQ.Socket> {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQAsyncConnectionPool.class);

    private boolean open = true;

    private final int min;

    private final int max;

    private final String name;

    private final Function<ZContext, ZMQ.Socket> socketSupplier;

    private final JeroMQAsyncConnectionService.SimpleAsyncConnectionServiceContext context;

    private final Lock lock = new ReentrantLock();

    private final Condition condition = lock.newCondition();

    private final Queue<JeroMQAsyncConnection> available = new LinkedList<>();

    private final List<JeroMQAsyncConnection> connections = new ArrayList<>();

    public JeroMQAsyncConnectionPool(final String name, final int min, final int max,
                                     final Function<ZContext, ZMQ.Socket> socketSupplier,
                                     final JeroMQAsyncConnectionService.SimpleAsyncConnectionServiceContext parentContext) {

        if (min >= max) throw new IllegalArgumentException("min must be < max");

        this.min = min;
        this.max = max;
        this.name = name;
        this.socketSupplier = socketSupplier;
        this.context = parentContext;

        parentContext.getThreadContextRoundRobin().forEach(c -> c.doInThread(() -> {
            c.onPostLoop((s, v) -> ensureMinimum(s, c));
        }));

    }

    private void ensureMinimum(final Subscription subscription, final JeroMQAsyncThreadContext context) {

        try {

            lock.lock();

            int added = 0;
            final int toAdd = max((min - connections.size()) / THREAD_POOL_SIZE, 0);

            while (open && connections.size() < min && connections.size() < max && (added++ < toAdd)) {
                final JeroMQAsyncConnection connection = context.allocateNewConnection(socketSupplier);
                doAddConnection(connection);
            }

            if (!open) subscription.unsubscribe();

        } finally {
            lock.unlock();
        }

    }

    @Override
    public void acquireNextAvailableConnection(final Consumer<AsyncConnection<ZContext, ZMQ.Socket>> asyncConnectionConsumer) {

        JeroMQAsyncConnection connection;

        requestConnection();

        try {

            lock.lock();

            while ((connection = available.poll()) == null) {
                checkOpen();
                condition.await();
            }

        } catch (InterruptedException e) {
            throw new InternalError(e);
        } finally {
            lock.unlock();
        }

        connection.signal(asyncConnectionConsumer);

    }

    private void requestConnection() {

        final boolean acquire;

        try {
            lock.lock();
            checkOpen();
            acquire = connections.size() < max;
        } finally {
            lock.unlock();
        }

        if (acquire) {

            final JeroMQAsyncThreadContext context = this.context.getThreadContextRoundRobin().getNext();

            context.doInThread(() -> {


                boolean close = true;
                final JeroMQAsyncConnection connection = context.allocateNewConnection(socketSupplier);

                try {

                    lock.lock();

                    if (open) {
                        close = false;
                        doAddConnection(connection);
                    }

                } finally {
                    lock.unlock();
                    if (close) connection.close();
                }

            });

        }

    }

    private void doAddConnection(final JeroMQAsyncConnection connection) {

        condition.signal();
        connections.add(connection);
        available.offer(connection);

        connection.onClose(c -> {
            try {
                lock.lock();
                logger.trace("Closed connection {}", c);
                if (open && available.remove(connection)) logger.warn("Should not have removed available connection {}", c);
                if (open && !connections.remove(connection)) logger.warn("Could not remove connection {}", c);
            } finally {
                lock.unlock();
            }
        });

        connection.onRecycle(c -> {
            try {

                lock.lock();
                condition.signalAll();

                connection.clearEvents();
                connection.getOnError().clear();
                connection.getOnRead().clear();
                connection.getOnWrite().clear();

                if (open && available.offer(connection)) {
                    logger.trace("Recycled connection {}", c);
                }

            } finally {
                lock.unlock();
            }
        });

    }

    private void checkOpen() {
        if (!open)
            throw new IllegalStateException("Not open.");
    }

    @Override
    public void close() {
        if (context.remove(this)) {
            doClose();
        } else {
            logger.warn("Already removed from the parent AsyncConnectionService: {}", this);
        }
    }

    public void doClose() {

        try {

            lock.lock();

            if (open) {
                open = false;
                condition.signalAll();
            }

            concat(available.stream(), connections.stream()).forEach(c -> c.signal(z -> {

                    c.getOnClose().clear();

                    try {
                        c.close();
                    } catch (UncheckedIOException ex) {
                        if (ex.getCause() instanceof AsynchronousCloseException) {
                            logger.debug("Socket already closed. Nothing to do.", ex);
                        } else {
                            throw ex;
                        }
                    }

                }
            ));

        } finally {
            lock.unlock();
        }


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
