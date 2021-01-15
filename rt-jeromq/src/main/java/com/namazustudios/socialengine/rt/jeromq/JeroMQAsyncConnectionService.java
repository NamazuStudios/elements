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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.Runtime.getRuntime;
import static java.lang.String.format;
import static java.lang.Thread.interrupted;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.zeromq.ZContext.shadow;

public class JeroMQAsyncConnectionService implements AsyncConnectionService<ZContext, ZMQ.Socket> {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQAsyncConnectionService.class);

    private static final int POLL_INTERVAL = 1000;

    static final int THREAD_POOL_SIZE = getRuntime().availableProcessors() + 1;

    private final AtomicReference<SimpleAsyncConnectionServiceContext> context = new AtomicReference<>();

    private ZContext zContext;

    @Override
    public void start() {

        final var context = new SimpleAsyncConnectionServiceContext();

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
    public AsyncConnectionGroup.Builder<ZContext, ZMQ.Socket> group(final String name) {
        final SimpleAsyncConnectionServiceContext context = getContext();
        return context.group(name);
    }

    @Override
    public AsyncConnectionPool<ZContext, ZMQ.Socket> allocatePool(
            final String name, final int minConnections, final int maxConnections,
            final Function<ZContext, ZMQ.Socket> socketSupplier) {
        final var context = getContext();
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

        private List<Thread> ioThreads;

        private RoundRobin<JeroMQAsyncThreadContext> threadContextRoundRobin;

        private final AtomicBoolean running = new AtomicBoolean(true);

        private final List<JeroMQAsyncConnectionPool> simpleManagedPoolList = new CopyOnWriteArrayList<>();

        private SimpleAsyncConnectionServiceContext() {}

        public void start() {

            final AtomicInteger threadCount = new AtomicInteger();

            final CountDownLatch latch = new CountDownLatch(THREAD_POOL_SIZE);
            threadContextRoundRobin = new ConcurrentRoundRobin<>(new JeroMQAsyncThreadContext[0], THREAD_POOL_SIZE);

            ioThreads = range(0, THREAD_POOL_SIZE).mapToObj(i -> {
                final String name = JeroMQAsyncConnectionService.class.getSimpleName() + " " + threadCount.getAndIncrement();
                final Thread thread = new Thread(() -> runIOThread(latch, i));
                thread.setDaemon(true);
                thread.setUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception in thread {}", t, e));
                thread.setName(name);
                thread.start();
                return thread;
            }).collect(toList());

            try {
                latch.await();
            } catch (InterruptedException ex) {
                throw new InternalException(ex);
            }

        }

        public void stop() {

            running.set(false);
            simpleManagedPoolList.forEach(JeroMQAsyncConnectionPool::doClose);

            ioThreads.forEach(Thread::interrupt);
            ioThreads.forEach(t -> {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    logger.error("Interrupted while joining IO thread {}", t);
                }
            });

        }

        private void runIOThread(final CountDownLatch latch, final int index) {
            try (final var shadow = shadow(getzContext());
                 final var poller = shadow.createPoller(1);
                 final var context = new JeroMQAsyncThreadContext(shadow, poller)) {

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
            } finally {
                logger.info("Exiting IO thread.");
            }
        }

        public AsyncConnectionGroup.Builder<ZContext, ZMQ.Socket> group(final String name) {
            return new AsyncConnectionGroup.Builder<>() {

                private final List<Function<JeroMQAsyncThreadContext, AsyncConnection<ZContext, ZMQ.Socket>>> connectionSupplierList = new ArrayList<>();

                @Override
                public AsyncConnectionGroup.Builder<ZContext, ZMQ.Socket>
                connection(final Function<ZContext, ZMQ.Socket> socketSupplier,
                           final Consumer<AsyncConnection<ZContext, ZMQ.Socket>> asyncConnectionConsumer) {

                    connectionSupplierList.add(context -> {
                        final var connection = context.allocateNewConnection(name, socketSupplier);
                        asyncConnectionConsumer.accept(connection);
                        connection.getOnRecycle().subscribe(c -> connection.close());
                        return connection;
                    });

                    return this;
                }

                @Override
                public void build(final Consumer<AsyncConnectionGroup<ZContext, ZMQ.Socket>> consumer) {

                    final JeroMQAsyncThreadContext context = threadContextRoundRobin.getNext();

                    context.doInThread(() -> {

                        final var connectionList = connectionSupplierList
                            .stream()
                            .map(supplier -> supplier.apply(context))
                            .collect(toList());

                        final var group = new JeroMQAsyncConnectionGroup(
                                connectionList,
                                (g, c) -> context.doInThread(() -> c.accept(g))) {

                            final String toString = format("%s - %s", super.toString(), name);

                            @Override
                            public String toString() {
                                return toString;
                            }

                        };

                        consumer.accept(group);

                    });

                }

            };

        }

        public AsyncConnectionPool<ZContext, ZMQ.Socket> allocatePool(
                final String name, final int minConnections, final int maxConnections,
                final Function<ZContext, ZMQ.Socket> socketSupplier) {
            final JeroMQAsyncConnectionPool pool = new JeroMQAsyncConnectionPool(
                name,
                minConnections, maxConnections,
                socketSupplier, this);
            simpleManagedPoolList.add(pool);
            return pool;
        }

        public boolean isRunning() {
            return running.get();
        }

        public RoundRobin<JeroMQAsyncThreadContext> getThreadContextRoundRobin() {
            return threadContextRoundRobin;
        }

        boolean remove(final JeroMQAsyncConnectionPool simpleManagedPool) {
            return simpleManagedPoolList.remove(simpleManagedPool);
        }

    }

}
