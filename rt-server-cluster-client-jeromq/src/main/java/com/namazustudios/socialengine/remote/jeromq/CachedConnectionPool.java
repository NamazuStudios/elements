package com.namazustudios.socialengine.remote.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static java.lang.Thread.interrupted;

public class CachedConnectionPool implements ConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(CachedConnectionPool.class);

    private static final String TIMEOUT = "com.namazustudios.socialengine.remote.jeromq.CachedConnectionPool.timeout";

    private static final String MIN_CONNECTIONS = "com.namazustudios.socialengine.remote.jeromq.CachedConnectionPool.minConnections";

    private final AtomicReference<Context> context = new AtomicReference<>();

    private ZContext zContext;

    private int timeout;

    private int minConnections;

    @Override
    public void start() {

        final Context c = new Context();

        if (context.compareAndSet(null, c)) {
            c.start();
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    @Override
    public void stop() {

        final Context c = context.getAndSet(null);

        if (c == null) {
            throw new IllegalStateException("Not started");
        } else {
            c.stop();
        }

    }

    @Override
    public void process(final Consumer<Connection> consumer) {

        final Context c = context.get();

        if (c != null) {
            c.process(consumer);
        } else {
            throw new IllegalStateException("Not started.");
        }

    }

    public int getTimeout() {
        return timeout;
    }

    @Inject
    public void setTimeout(@Named(TIMEOUT) int timeout) {
        this.timeout = timeout;
    }

    public int getMinConnections() {
        return minConnections;
    }

    @Inject
    public void setMinConnections(@Named(MIN_CONNECTIONS) int minConnections) {
        this.minConnections = minConnections;
    }

    public ZContext getzContext() {
        return zContext;
    }

    @Inject
    public void setzContext(ZContext zContext) {
        this.zContext = zContext;
    }

    private class Context {

        private final int timeout = getTimeout();

        private final int minConnections = getMinConnections();

        private final AtomicInteger count = new AtomicInteger();

        private final AtomicBoolean running = new AtomicBoolean();

        private final BlockingQueue<Consumer<Connection>> workQueue = new LinkedBlockingDeque<>();

        private final Set<Thread> threadSet = new ConcurrentHashMap<Thread, Object>().keySet(new Object());

        public void start() {
            if (running.compareAndSet(false, true)) {
                doStart();
            } else {
                throw new IllegalStateException("Already started.");
            }
        }

        private void doStart() {
            for (int i = 0; i < minConnections; ++i) {
                startNewWorker();
            }
        }

        public void stop() {
            if (running.compareAndSet(true, false)) {
                doStop();
            } else {
                throw new IllegalStateException("Already stopped.");
            }
        }

        private void doStop() {
            // TODO Implement this
        }

        public void process(final Consumer<Connection> consumer) {
            if (running.get()) {
                doProcess(consumer);
            } else {
                throw new IllegalStateException("Not Running.");
            }
        }

        private void doProcess(Consumer<Connection> consumer) {
            // TODO Implement this
        }

        private void startNewWorker() {

            final Worker worker = new Worker();
            final Thread thread = new Thread(worker);

            thread.setDaemon(true);
            thread.setName(CachedConnectionPool.class.getSimpleName() + " worker.");

            threadSet.add(thread);
            thread.start();

        }

        private class Worker implements Runnable {

            @Override
            public void run() {

                final ZContext zContext = getzContext();
                count.incrementAndGet();

                try (final ZMQ.Socket socket = zContext.createSocket(ZMQ.DEALER)) {

                    final Connection connection = new Connection() {

                        @Override
                        public ZContext context() {
                            return zContext;
                        }

                        @Override
                        public ZMQ.Socket socket() {
                            return socket;
                        }

                    };

                    while (running.get() && count.get() < minConnections) {
                        consumeWorkUntilTimeout(connection);
                    }

                    logger.info("Shutting down connection.");

                } finally {
                    count.decrementAndGet();
                }

            }

            private void consumeWorkUntilTimeout(final Connection connection) {
                for (Consumer<Connection> c = waitForWork(); running.get() && c != null; c = waitForWork()) {
                    c.accept(connection);
                }
            }

            private Consumer<Connection> waitForWork() {
                try {
                    return workQueue.poll(timeout, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    logger.info("Interrupted processing work.  Shutting down connection.", ex);
                    return null;
                } catch (Throwable th) {
                    logger.error("Caught exception in connection pool.", th);
                    return null;
                }
            }

        }

    }

    private static class TerminalConnection  implements Connection {

        @Override
        public ZContext context() {
            throw new IllegalStateException("Connection pool shutdown.");
        }

        @Override
        public ZMQ.Socket socket() {
            throw new IllegalStateException("Connection pool shutdown.");
        }

    }

}
