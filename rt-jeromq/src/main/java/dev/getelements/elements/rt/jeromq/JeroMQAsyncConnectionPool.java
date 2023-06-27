package dev.getelements.elements.rt.jeromq;

import dev.getelements.elements.rt.AsyncConnection;
import dev.getelements.elements.rt.AsyncConnectionPool;
import dev.getelements.elements.rt.Connection;
import dev.getelements.elements.rt.Subscription;
import dev.getelements.elements.rt.exception.MultiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.collect.Streams.concat;
import static java.lang.Math.max;
import static java.util.Collections.emptyList;
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
            final int toAdd = max((min - connections.size()) / this.context.getThreadPoolSize(), 0);

            while (open && connections.size() < min && connections.size() < max && (added++ < toAdd)) {
                final var connection = context.allocateNewConnection(name, socketSupplier);
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
            checkOpen();

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
            acquire = available.isEmpty() && connections.size() < max;
        } finally {
            lock.unlock();
        }

        if (acquire) {

            final var context = this.context.getThreadContextRoundRobin().getNext();

            context.doInThread(() -> {

                boolean close = true;
                final var connection = context.allocateNewConnection(name, socketSupplier);

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

        condition.signalAll();
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
        if (!open) throw new IllegalStateException("Not open.");
    }

    @Override
    public void close() {
        if (context.remove(this)) {
            doClose();
        } else {
            logger.warn("Already removed from the parent AsyncConnectionService: {}", this);
        }
    }

    /**
     * Implementation detail. Do not call directly.
     */
    void doClose() {

        final List<JeroMQAsyncConnection> toClose;

        try {
            lock.lock();
            condition.signalAll();
            toClose = open ? emptyList() : concat(available.stream(), connections.stream()).collect(toList());
        } finally {
            open = false;
            lock.unlock();
        }

        final var causes = new ArrayList<Exception>();
        final var initial = new CompletableFuture<Void>();

        var completion = toClose.stream().map(connection -> {
            try {
                return connection.signalAndComputeCompletionV(Connection::close);
            } catch (UncheckedIOException ex) {
                final var future = new CompletableFuture<>();
                future.completeExceptionally(ex.getCause());
                logger.error("Caught IO Exception closing connection pool.", ex);
                causes.add(ex.getCause());
                return future;
            } catch (Exception ex) {
                final var future = new CompletableFuture<>();
                future.completeExceptionally(ex.getCause());
                logger.error("Caught Exception closing connection pool.", ex);
                causes.add(ex);
                return future;
            }
        }).reduce(initial, (a, b) -> a.thenCombineAsync(b, (o, o2) -> null));

        try {
            initial.complete(null);
            completion.toCompletableFuture().get();
        } catch (InterruptedException ex) {
            logger.info("Interrupted while closing.", ex);
        } catch (ExecutionException ex) {
            causes.add(ex);
        }

        if (!causes.isEmpty()) throw new MultiException(causes);

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
