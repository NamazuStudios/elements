package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.Runtime.getRuntime;
import static java.lang.Thread.interrupted;
import static java.util.concurrent.ConcurrentHashMap.newKeySet;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.Poller.*;

public class SimpleAsyncConnectionService implements AsyncConnectionService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleAsyncConnectionService.class);

    private static final int POLL_INTERVAL = 1000;

    private static final int THREAD_POOL_SIZE = getRuntime().availableProcessors() + 1;

    private final AtomicReference<SimpleAsyncConnectionServiceContext> context = new AtomicReference<>();

    private ZContext zContext;

    @Override
    public void start() {

        final SimpleAsyncConnectionServiceContext context = new SimpleAsyncConnectionServiceContext();

        if (this.context.compareAndSet(null, context)) {
            context.start();
        } else {
            throw new IllegalArgumentException("Already running.");
        }

    }

    @Override
    public void stop() {

        final SimpleAsyncConnectionServiceContext context = this.context.getAndSet(null);

        if (context == null) {
            throw new IllegalStateException("Not running.");
        } else {
            context.stop();
        }

    }

    @Override
    public void open(final Function<ZContext, ZMQ.Socket> socketSupplier,
                     final Consumer<AsyncConnection> asyncConnectionConsumer) {
        final SimpleAsyncConnectionServiceContext context = getContext();
        context.open(socketSupplier, asyncConnectionConsumer);
    }

    @Override
    public Pool allocatePool(
            final String name,
            final int minConnections, final int maxConnextions,
            final Function<ZContext, ZMQ.Socket> socketSupplier) {
        final SimpleAsyncConnectionServiceContext context = getContext();
        return context.allocatePool(name, minConnections, maxConnextions, socketSupplier);
    }

    private SimpleAsyncConnectionServiceContext getContext() {
        final SimpleAsyncConnectionServiceContext context = this.context.get();
        if (context == null) throw new IllegalArgumentException();
        return context;
    }

    public ZContext getzContext() {
        return zContext;
    }

    @Inject
    public void setzContext(ZContext zContext) {
        this.zContext = zContext;
    }

    private class SimpleAsyncConnectionServiceContext {

        private ExecutorService executor;

        private ThreadGroup threadGroup;

        private RoundRobin<ThreadContext> threadContextRoundRobin;

        private final AtomicBoolean running = new AtomicBoolean(true);

        private final List<SimpleManagedPool> simpleManagedPoolList = new CopyOnWriteArrayList<>();

        public void start() {

            final AtomicInteger threadCount = new AtomicInteger();
            threadGroup = new ThreadGroup(SimpleAsyncConnectionService.class.getSimpleName());

            executor = Executors
                .newFixedThreadPool(THREAD_POOL_SIZE, r -> {
                    final String name = SimpleAsyncConnectionService.class.getSimpleName() + " " + threadCount.incrementAndGet();
                    final Thread thread = new Thread(threadGroup, r);
                    thread.setDaemon(true);
                    thread.setUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception in thread {}", e));
                    thread.setName(name);
                    return thread;
                });

            final CountDownLatch latch = new CountDownLatch(THREAD_POOL_SIZE);
            threadContextRoundRobin = new ConcurrentRoundRobin<>(new ThreadContext[0], THREAD_POOL_SIZE);
            range(0, THREAD_POOL_SIZE).forEach(i -> executor.submit(() -> runIOThread(latch, i)));

            try {
                latch.await();
            } catch (InterruptedException ex) {
                throw new InternalException(ex);
            }

        }

        public void stop() {

            simpleManagedPoolList.forEach(pool -> pool.doClose());

            running.set(false);
            executor.shutdown();

            try {
                executor.awaitTermination(1, MINUTES);
            } catch (InterruptedException ex) {
                throw new InternalException(ex);
            }

            threadGroup.destroy();

        }

        private void runIOThread(final CountDownLatch latch, final int index) {
            try (final ZContext shadow = shadow(getzContext());
                 final ZMQ.Poller poller = shadow.createPoller(1);
                 final ThreadContext context = new ThreadContext(shadow, poller)) {

                threadContextRoundRobin.set(index, context);
                latch.countDown();

                while (running.get()) {

                    if (poller.poll(POLL_INTERVAL) < 0 || interrupted()) {
                        logger.info("Got interrupt signal.");
                        break;
                    }

                    context.poll();

                }

            } catch (Exception ex) {
                logger.error("Uncaught exception in IO thread.", ex);
                throw ex;
            }finally {
                logger.info("Exiting IO thread.");
            }
        }

        public void open(final Function<ZContext, ZMQ.Socket> socketSupplier,
                         final Consumer<AsyncConnection> asyncConnectionConsumer) {

            final ThreadContext context = threadContextRoundRobin.getNext();

            context.doInThread(() -> {
                final ConnectionHandle handle = context.allocateNewConnection(socketSupplier);
                final AsyncConnection asyncConnection = context.getConnection(context.commandIndex);
            });

        }

        public Pool allocatePool(
                final String name,
                final int minConnections, final int maxConnextions,
                final Function<ZContext, ZMQ.Socket> socketSupplier) {
            final SimpleManagedPool pool = new SimpleManagedPool(
                name, minConnections, maxConnextions,
                socketSupplier, this);
            simpleManagedPoolList.add(pool);
            return pool;
        }

    }

    private static class ThreadContext implements AutoCloseable {

        private final int commandIndex;

        private final int connectionIndexStart;

        private final Pipe pipe;

        private final ZMQ.Poller poller;

        private final ZContext zContext;

        private final Lock writeLock = new ReentrantLock();

        private final AtomicInteger next = new AtomicInteger();

        private final Publisher<Void> onPostLoop = new SimplePublisher<>();

        private final ByteBuffer incoming = ByteBuffer.allocate(Integer.BYTES);

        private final SortedMap<Integer, Runnable> commands = new ConcurrentSkipListMap<>();

        public ThreadContext(final ZContext zContext, final ZMQ.Poller poller) {

            try {
                this.pipe = Pipe.open();
                this.pipe.source().configureBlocking(false);
            } catch (IOException e) {
                throw new InternalException(e);
            }

            this.poller = poller;
            this.commandIndex = poller.register(pipe.source(), (POLLIN | POLLERR));
            this.connectionIndexStart = poller.getNext();
            this.zContext = zContext;

        }

        private void doInThread(final Runnable command) {

            final int index = next.getAndIncrement();
            final ByteBuffer output = ByteBuffer.allocate(Integer.BYTES);

            commands.put(index, command);
            output.putInt(index);
            output.flip();

            try {
                writeLock.lock();
                while(output.remaining() > 0) pipe.sink().write(output);
            } catch (IOException ex) {
                throw new InternalException(ex);
            } finally {
                writeLock.unlock();
            }

        }

        public void poll() {
            pollCommands();
            pollManagedConnections();
            onPostLoop.publish(null);
        }

        public void pollCommands() {

            if (!poller.pollin(commandIndex)) return;
            incoming.position(0).limit(Integer.BYTES);

            try {
                while (pipe.source().read(incoming) > 0) {
                    if (incoming.remaining() == 0) {
                        incoming.flip();
                        process(incoming.getInt());
                        incoming.flip();
                    }
                }
            } catch (IOException ex) {
                throw new InternalException(ex);
            }

        }

        public void pollManagedConnections() {

            final int next = poller.getNext();

            for (int index = connectionIndexStart; index < next; ++index) {
                final SimpleAsyncConnection connection = (SimpleAsyncConnection) poller.getItem(index);
                if (connection != null) {
                    if (poller.pollin(index)) connection.getOnRead().publish(connection);
                    if (poller.pollout(index)) connection.getOnWrite().publish(connection);
                    if (poller.pollerr(index)) connection.getOnError().publish(connection);
                }
            }

        }

        private void process(final int command) {
            final Runnable runnable = commands.remove(command);
            if (runnable == null) {
                logger.error("Unable to process command at index {}", command);
            } else {
                try {
                    runnable.run();
                } catch (Exception ex) {
                    logger.error("Caught exception processing command {}.", runnable, ex);
                }
            }
        }

        @Override
        public void close() {

            try {
                pipe.sink().close();
            } catch (IOException ex) {
                logger.error("Error closing sink.", ex);
            }

            try {
                pipe.source().close();
            } catch (IOException ex) {
                logger.error("Error closing source.", ex);
            }

        }

        public ConnectionHandle allocateNewConnection(final Function<ZContext, ZMQ.Socket> socketSupplier) {

            final ZMQ.Socket socket = socketSupplier.apply(zContext);

            final SimpleAsyncConnection connection = new SimpleAsyncConnection(
                    zContext, socket,
                    (conn, consumer) -> doInThread(() -> consumer.accept(conn)));

            connection.onClose(c -> onPostLoop.subscribe((subscriber, v) -> {
                poller.unregister(socket);
                socket.close();
                subscriber.unsubscribe();
            }));

            final int index = poller.register(connection);
            return new ConnectionHandle(index, this);

        }

        public SimpleAsyncConnection getConnection(final int index) {
            return (SimpleAsyncConnection) poller.getItem(index);
        }

        public Subscription onPostLoop(final BiConsumer<Subscription, Void> consumer) {
            return onPostLoop.subscribe(consumer);
        }

    }

    private static class SimpleManagedPool implements Pool {

        private final int min;

        private final int max;

        private final String name;

        private final Semaphore semaphore;

        private final Function<ZContext, ZMQ.Socket> socketSupplier;

        private final SimpleAsyncConnectionServiceContext context;

        private final AtomicBoolean open = new AtomicBoolean(true);

        private final Set<ConnectionHandle> connectionHandles = newKeySet();

        private final Queue<ConnectionHandle> available = new ConcurrentLinkedQueue<>();

        public SimpleManagedPool(final String name, final int min, final int max,
                                 final Function<ZContext, ZMQ.Socket> socketSupplier,
                                 final SimpleAsyncConnectionServiceContext parentContext) {
            this.min = min;
            this.max = max;
            this.name = name;
            this.socketSupplier = socketSupplier;
            this.context = parentContext;
            this.semaphore = new Semaphore(max);
            parentContext.threadContextRoundRobin.forEach(c -> c.onPostLoop((s, v) -> ensureMinimum(s, c)));
        }

        private void ensureMinimum(final Subscription subscription, final ThreadContext context) {
            if (open.get()) {

                int added = 0;

                while (connectionHandles.size() < min && (added++ < (min / THREAD_POOL_SIZE))) {
                    final ConnectionHandle handle = context.allocateNewConnection(socketSupplier);
                    final SimpleAsyncConnection connection = context.getConnection(handle.index);
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
        public void acquireNextAvailableConnection(final Consumer<AsyncConnection> asyncConnectionConsumer) {

            if (!open.get()) throw new IllegalStateException("Pool is closed.");

            try {
                semaphore.acquire();
            } catch (InterruptedException ex) {
                throw new InternalException(ex);
            }

            final ConnectionHandle entry = available.poll();

            if (entry == null) {
                doAcqureNew(asyncConnectionConsumer);
            } else {
                doReuseConnection(asyncConnectionConsumer, entry);
            }

        }

        private void doAcqureNew(final Consumer<AsyncConnection> asyncConnectionConsumer) {

            final ThreadContext context = this.context.threadContextRoundRobin.getNext();

            context.doInThread(() -> {
                final ConnectionHandle handle = context.allocateNewConnection(socketSupplier);
                final SimpleAsyncConnection connection = context.getConnection(handle.index);
                addConnection(handle, connection);
                asyncConnectionConsumer.accept(connection);
            });

        }

        private void addConnection(final ConnectionHandle handle, final SimpleAsyncConnection connection) {

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

        private void doReuseConnection(final Consumer<AsyncConnection> asyncConnectionConsumer,
                                       final ConnectionHandle handle) {
            handle.context.doInThread(() -> {
                final AsyncConnection connection = handle.context.getConnection(handle.index);
                asyncConnectionConsumer.accept(connection);
            });
        }

        @Override
        public void close() {
            if (!open.compareAndSet(true, false)) throw new IllegalStateException("Pool is closed.");
            context.simpleManagedPoolList.remove(this);
            doClose();
        }

        public void doClose() {

            try {
                semaphore.acquire(max);
            } catch (InterruptedException e) {
                logger.error("Could not acquire all remaining connections.", e);
            }

            connectionHandles.stream().collect(toList()).forEach(ch -> {
                ch.context.doInThread(() -> {
                    final AsyncConnection connection = ch.context.getConnection(ch.index);
                    connection.close();
                });
            });

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

    private static class ConnectionHandle implements Comparable<ConnectionHandle> {

        public final int index;

        public final ThreadContext context;

        public ConnectionHandle(final int index, final ThreadContext context) {
            this.index = index;
            this.context = context;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ConnectionHandle)) return false;
            ConnectionHandle handle = (ConnectionHandle) o;
            return index == handle.index && context.equals(handle.context);
        }

        @Override
        public int hashCode() {
            return Objects.hash(index, context);
        }

        @Override
        public int compareTo(final ConnectionHandle other) {
            return Integer.compare(index, other.index);
        }

    }

}
