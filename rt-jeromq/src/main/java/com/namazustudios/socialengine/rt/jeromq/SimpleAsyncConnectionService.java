package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.Runtime.getRuntime;
import static java.lang.Thread.interrupted;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.zeromq.ZContext.shadow;

public class SimpleAsyncConnectionService implements AsyncConnectionService<ZContext, ZMQ.Socket> {

    private static final Logger logger = LoggerFactory.getLogger(SimpleAsyncConnectionService.class);

    private static final int POLL_INTERVAL = 1000;

    static final int THREAD_POOL_SIZE = getRuntime().availableProcessors() + 1;

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
    public AsyncConnectionGroup.Builder<ZContext, ZMQ.Socket> group() {
        final SimpleAsyncConnectionServiceContext context = getContext();
        return context.group();
    }

    @Override
    public AsyncConnectionPool<ZContext, ZMQ.Socket> allocatePool(
            final String name,
            final int minConnections, final int maxConnections,
            final Function<ZContext, ZMQ.Socket> socketSupplier) {
        final SimpleAsyncConnectionServiceContext context = getContext();
        return context.allocatePool(name, minConnections, maxConnections, socketSupplier);
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

    class SimpleAsyncConnectionServiceContext {

        private ExecutorService executor;

        private ThreadGroup threadGroup;

        private RoundRobin<SimpleAsyncThreadContext> threadContextRoundRobin;

        private final AtomicBoolean running = new AtomicBoolean(true);

        private final List<SimpleAsyncConnectionPool> simpleManagedPoolList = new CopyOnWriteArrayList<>();

        private SimpleAsyncConnectionServiceContext() {}

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
            threadContextRoundRobin = new ConcurrentRoundRobin<>(new SimpleAsyncThreadContext[0], THREAD_POOL_SIZE);
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
                 final SimpleAsyncThreadContext context = new SimpleAsyncThreadContext(shadow, poller)) {

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

        public AsyncConnectionGroup.Builder<ZContext, ZMQ.Socket> group() {
            return new AsyncConnectionGroup.Builder<ZContext, ZMQ.Socket>() {

                private List<Function<SimpleAsyncThreadContext, AsyncConnection>> connectionSupplierList = new ArrayList<>();

                @Override
                public AsyncConnectionGroup.Builder<ZContext, ZMQ.Socket>
                        connection(final Function<ZContext, ZMQ.Socket> socketSupplier,
                                   final Consumer<AsyncConnection<ZContext, ZMQ.Socket>> asyncConnectionConsumer) {

                    connectionSupplierList.add(context -> {
                        final SimpleAsyncConnectionHandle handle = context.allocateNewConnection(socketSupplier);
                        final SimpleAsyncConnection connection = context.getConnection(handle.index);
                        asyncConnectionConsumer.accept(connection);
                        connection.getOnRecycle().subscribe(c -> connection.close());
                        return connection;
                    });

                    return this;
                }

                @Override
                public void build(final Consumer<AsyncConnectionGroup<ZContext, ZMQ.Socket>> consumer) {

                    final SimpleAsyncThreadContext context = threadContextRoundRobin.getNext();

                    context.doInThread(() -> {

                        final List<AsyncConnection> connectionList = connectionSupplierList
                            .stream()
                            .map(supplier -> supplier.apply(context))
                            .collect(toList());

                        final SimpleAsyncConnectionGroup group = new SimpleAsyncConnectionGroup(
                            connectionList,
                            (g, c) -> context.doInThread(() -> c.accept(g)));

                        consumer.accept(group);

                    });

                }

            };



        }

        public AsyncConnectionPool allocatePool(
                final String name,
                final int minConnections, final int maxConnextions,
                final Function<ZContext, ZMQ.Socket> socketSupplier) {
            final SimpleAsyncConnectionPool pool = new SimpleAsyncConnectionPool(
                name, minConnections, maxConnextions,
                socketSupplier, this);
            simpleManagedPoolList.add(pool);
            return pool;
        }

        public boolean isRunning() {
            return running.get();
        }

        public RoundRobin<SimpleAsyncThreadContext> getThreadContextRoundRobin() {
            return threadContextRoundRobin;
        }

        void remove(final SimpleAsyncConnectionPool simpleManagedPool) {
            simpleManagedPoolList.remove(simpleManagedPool);
        }

    }

}
