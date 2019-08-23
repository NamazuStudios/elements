package com.namazustudios.socialengine.rt.jeromq;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.namazustudios.socialengine.rt.ConcurrentRoundRobin;
import com.namazustudios.socialengine.rt.RoundRobin;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
    public ManagedPool allocatePool(
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

        private RoundRobin<ThreadContext> threadContextRoundRobin;

        private final AtomicBoolean running = new AtomicBoolean(true);

        private final List<SimpleManagedPool> simpleManagedPoolList = new CopyOnWriteArrayList<>();

        public void start() {

            final AtomicInteger threadCount = new AtomicInteger();

            executor = Executors
                .newFixedThreadPool(THREAD_POOL_SIZE, r -> {
                    final String name = SimpleAsyncConnectionService.class.getSimpleName() + " " + threadCount.incrementAndGet();
                    final Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    thread.setUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception in thread {}", e));
                    thread.setName(name);
                    return thread;
                });

            threadContextRoundRobin = new ConcurrentRoundRobin<>(new ThreadContext[0], THREAD_POOL_SIZE);

            final CountDownLatch latch = new CountDownLatch(THREAD_POOL_SIZE);
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
            executor.shutdownNow();

            try {
                executor.awaitTermination(5, MINUTES);
            } catch (InterruptedException ex) {
                throw new InternalException(ex);
            }

        }

        private void runIOThread(final CountDownLatch latch, final int index) {
            try (final ZContext shadow = shadow(getzContext());
                 final ZMQ.Poller poller = shadow.createPoller(1);
                 final ThreadContext context = threadContextRoundRobin.set(index, new ThreadContext(shadow, poller))) {

                latch.countDown();

                while (!running.get()) {

                    if (poller.poll(POLL_INTERVAL) < 0 || interrupted()) {
                        logger.info("Got interrupt signal.");
                        break;
                    }

                    context.poll();

                }

            } finally {
                logger.info("Exiting IO thread.");
            }
        }

        public ManagedPool allocatePool(
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

        private final int index;

        private final Pipe pipe;

        private final ZMQ.Poller poller;

        private final ZContext zContext;

        private final AtomicInteger next = new AtomicInteger();

        private final ByteBuffer incoming = ByteBuffer.allocate(Integer.BYTES);

        private final Map<Integer, Runnable> commands = new ConcurrentSkipListMap<>();

        private final BiMap<Integer, SimpleAsyncConnection> asyncConnectionMap = HashBiMap.create();

        private final BiMap<SimpleAsyncConnection, Integer> rAsyncConnectionMap = asyncConnectionMap.inverse();

        public ThreadContext(final ZContext zContext, final ZMQ.Poller poller) {

            try {
                this.pipe = Pipe.open();
            } catch (IOException e) {
                throw new InternalException(e);
            }

            this.poller = poller;
            this.index = poller.register(pipe.source());
            this.zContext = zContext;

        }

        private void doInThread(final Runnable command) {

            final int index = next.getAndIncrement();
            final ByteBuffer output = ByteBuffer.allocate(Integer.BYTES);

            commands.put(index, command);
            output.putInt(index);
            output.flip();

            try {
                pipe.sink().write(output);
            } catch (IOException ex) {
                throw new InternalException(ex);
            }

        }

        public void poll() {

            if (!poller.pollin(index)) return;
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

            asyncConnectionMap.forEach((index, connection) -> {
                if (poller.pollin(index)) connection.getOnRead().publish(connection);
                if (poller.pollout(index)) connection.getOnWrite().publish(connection);
                if (poller.pollerr(index)) connection.getOnError().publish(connection);
            });

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
            final int index = poller.register(socket, POLLIN| POLLOUT|POLLERR);
            final SimpleAsyncConnection connection = new SimpleAsyncConnection(zContext, socket);

            connection.getOnClose().subscribe(c -> {
                poller.unregister(socket);
                rAsyncConnectionMap.remove(connection);
            });

            return new ConnectionHandle(index, this);

        }

        public SimpleAsyncConnection getConnection(final int index) {
            return asyncConnectionMap.get(index);
        }

    }

    private static class SimpleManagedPool implements ManagedPool {

        private final int min;

        private final int max;

        private final String name;

        private final Semaphore semaphore;

        private final Function<ZContext, ZMQ.Socket> socketSupplier;

        private final SimpleAsyncConnectionServiceContext context;

        private final AtomicBoolean open = new AtomicBoolean();

        private final Set<ConnectionHandle> connections = newKeySet();

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
                connections.add(handle);

                connection.getOnClose().subscribe(c -> connections.remove(handle));

                connection.getOnRecycle().subscribe(c -> {
                    available.add(handle);
                    semaphore.release();
                });

                asyncConnectionConsumer.accept(connection);

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

            connections.stream().collect(toList()).forEach(ch -> {
                ch.context.doInThread(() -> {
                    final AsyncConnection connection = ch.context.getConnection(ch.index);
                    connection.close();
                });
            });

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

    private static class ConnectionHandle {

        public final int index;

        public final ThreadContext context;

        public ConnectionHandle(final int index, final ThreadContext context) {
            this.index = index;
            this.context = context;
        }

    }

}
