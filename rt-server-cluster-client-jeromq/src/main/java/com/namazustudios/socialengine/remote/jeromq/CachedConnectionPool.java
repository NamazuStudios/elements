package com.namazustudios.socialengine.remote.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class CachedConnectionPool implements ConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(CachedConnectionPool.class);

    private static final String TIMEOUT = "com.namazustudios.socialengine.remote.jeromq.CachedConnectionPool.timeout";

    private static final String HIGH_WATER_MARK = "com.namazustudios.socialengine.remote.jeromq.CachedConnectionPool.highWaterMark";

    private static final String MIN_CONNECTIONS = "com.namazustudios.socialengine.remote.jeromq.CachedConnectionPool.minConnections";

    private final AtomicReference<Context> context = new AtomicReference<>();

    private ZContext zContext;

    private int timeout;

    private int highWaterMark;

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

    public int getHighWaterMark() {
        return highWaterMark;
    }

    @Inject
    public void setHighWaterMark(@Named(HIGH_WATER_MARK) int highWaterMark) {
        this.highWaterMark = highWaterMark;
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

        private final int highWaterMark = getHighWaterMark();

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

            final List<Consumer<Connection>> leftoverConsumerList = new ArrayList<>();
            workQueue.drainTo(leftoverConsumerList);

            final TerminalConnection terminalConnection = new TerminalConnection();

            leftoverConsumerList.forEach(consumer -> {
                try {
                    consumer.accept(terminalConnection);
                } catch (TerminatedException ex) {
                    // Expected exception.
                    logger.trace("Termianted connection.", ex);
                } catch (Throwable th) {
                    logger.error("Caught exception terminating connection.", th);
                }
            });

            threadSet.forEach(thread -> thread.interrupt());

            for (final Thread thread : threadSet) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    logger.info("Interrupted while joining threads.", e);
                    break;
                }
            }

        }

        public void process(final Consumer<Connection> consumer) {
            if (running.get()) {
                doProcess(consumer);
            } else {
                throw new IllegalStateException("Not Running.");
            }
        }

        private void doProcess(Consumer<Connection> consumer) {

            workQueue.add(consumer);

            if (workQueue.size() > highWaterMark) {
                startNewWorker();
            }

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

                count.incrementAndGet();

                try (final WorkerConnection connection = new WorkerConnection()) {

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
            throw new TerminatedException();
        }

        @Override
        public ZMQ.Socket socket() {
            throw new TerminatedException();
        }

    }

    private class WorkerConnection implements Connection, AutoCloseable {

        private final ZContext zContext = getzContext();

        private final ZMQ.Socket socket = zContext.createSocket(ZMQ.DEALER);

        @Override
        public ZContext context() {
            return zContext;
        }

        @Override
        public ZMQ.Socket socket() {
            return socket;
        }

        @Override
        public void close() {
            socket().close();
        }

    }

    private static class TerminatedException extends RuntimeException {
        public TerminatedException() {
            super("Connection pool terminated.");
        }
    }


}
