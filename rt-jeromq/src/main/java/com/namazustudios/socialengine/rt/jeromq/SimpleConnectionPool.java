package com.namazustudios.socialengine.rt.jeromq;

import com.google.common.base.Stopwatch;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import zmq.util.Errno;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Closeable;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.namazustudios.socialengine.rt.jeromq.Connection.from;
import static java.lang.Math.max;
import static java.lang.Math.round;
import static java.lang.Thread.interrupted;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.zeromq.ZContext.shadow;

public class SimpleConnectionPool implements ConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(SimpleConnectionPool.class);

    private static final int REPORT_INTERVAL = 30;

    private static final int WARNING_PERCENTAGE = 75;

    private final AtomicInteger highWaterMark = new AtomicInteger();

    private final AtomicReference<Context> context = new AtomicReference<>();

    private ZContext zContext;

    private int timeout;

    private int minConnections;

    private int maxConnections;

    @Override
    public void start(final Function<ZContext, ZMQ.Socket> socketSupplier, final String name) {

        final Context c = new Context(socketSupplier, name);

        if (context.compareAndSet(null, c)) {
            logger.info("Starting ...");
            c.start();
            logger.info("Started.");
        } else {
            c.stop();
            throw new IllegalStateException("Already started.");
        }

    }

    @Override
    public void stop() {

        final Context c = context.getAndSet(null);

        if (c == null) {
            throw new IllegalStateException("Not started");
        } else {
            logger.info("Stopping: {}", c);
            c.stop();
            logger.info("Stopped: {}", c);
        }

    }

    @Override
    public <T> T process(final Function<Connection, T> consumer) {

        final Context c = context.get();

        if (c == null) {
            throw new IllegalStateException("Not started.");
        } else {
            return c.process(consumer);
        }

    }

    @Override
    public int getHighWaterMark() {
        return highWaterMark.get();
    }

    public int getTimeout() {
        return timeout;
    }

    @Inject
    public void setTimeout(@Named(ConnectionPool.TIMEOUT) int timeout) {
        this.timeout = timeout;
    }

    public int getMinConnections() {
        return minConnections;
    }

    @Inject
    public void setMinConnections(@Named(ConnectionPool.MIN_CONNECTIONS) int minConnections) {
        this.minConnections = minConnections;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    @Inject
    public void setMaxConnections(@Named(ConnectionPool.MAX_CONNECTIONS) int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public ZContext getzContext() {
        return zContext;
    }

    @Inject
    public void setzContext(ZContext zContext) {
        this.zContext = zContext;
    }

    private class Context {

        private Pool pool;

        private Thread lifecycle;

        private final String name;

        private final Function<ZContext, ZMQ.Socket> socketSupplier;

        private final AtomicBoolean running = new AtomicBoolean(true);

        public Context(final Function<ZContext, ZMQ.Socket> socketSupplier, final String name) {
            this.name = name;
            this.socketSupplier = socketSupplier;
        }

        public void start() {

            final Exchanger<Pool> exchanger = new Exchanger();

            lifecycle = new Thread(() -> {
                try (final Pool pool = new Pool(socketSupplier)) {
                    exchanger.exchange(pool);
                    pool.run();
                } catch (InterruptedException ex) {
                    logger.info("Interrupted.  Exiting.", ex);
                }
            });

            lifecycle.setName(SimpleConnectionPool.class.getSimpleName() +" " + name + " lifecycle ");
            lifecycle.setUncaughtExceptionHandler((t, ex) -> logger.error("Lifecycle thread threw exception.", ex));
            lifecycle.start();

            try {
                pool = exchanger.exchange(null);
            } catch (InterruptedException ex) {
                logger.error("Interrupted during startup.", ex);
                throw new InternalException(ex);
            }

        }

        public void stop() {

            if (!running.compareAndSet(true, false)) throw new IllegalStateException("Already stopped.");
            lifecycle.interrupt();

            try {
                lifecycle.join();
            } catch (InterruptedException ex) {
                throw new InternalException(ex);
            }

        }

        public <T> T process(final Function<Connection, T> connectionTFunction) {

            if (!running.get()) throw new IllegalStateException("Already stopped.");

            final Connection connection = pool.take();

            try {
                final PooledConnection pooled = new PooledConnection(connection);
                final T result = connectionTFunction.apply(pooled);
                pool.offer(connection);
                return result;
            } catch (Throwable th) {
                pool.kill(connection);
                throw th;
            }

        }

        @Override
        public String toString() {
            return "Context{" +
                    "name='" + name + '\'' +
                    '}';
        }

    }

    private class Pool implements Closeable {

        private int count = 0;

        private final int min = getMinConnections();

        private final int max = getMaxConnections();

        private final int timeout = getTimeout();

        private final ZContext shadow = shadow(getzContext());

        private final Semaphore semaphore = new Semaphore(0, true);

        private final Queue<Connection> available = new ConcurrentLinkedQueue<>();

        private final BlockingQueue<Runnable> requests = new LinkedBlockingQueue<>();

        private final Function<ZContext, ZMQ.Socket> socketSupplier;

        public Pool(final Function<ZContext, ZMQ.Socket> socketSupplier) {
            this.socketSupplier = socketSupplier;
        }

        public void offer(final Connection connection) {
            available.add(connection);
            semaphore.release();
        }

        public Connection take() {
            try {
                if (semaphore.tryAcquire()) {
                    // Success!  We have a Socket from the pool.  Dequeue it and return the connection
                    // so it may be of use to the client code.
                    return available.remove();
                } else {

                    // We must wait for a socket.  We submit our best-effort attempt to get the background
                    // thread to give us a socket.  In which case we ensure that the pool has not exceeded
                    // its open socket count and we simply make a new one.  Otherwise, we must wait on an
                    // additional thread to return a Socket to the queue

                    requests.add(() -> {
                        if (count < max) {
                            final Connection connection = from(shadow, socketSupplier);
                            ++count;
                            offer(connection);

                            final int fill = round( ((float) count / (float) max) * 100f);
                            logger.info("Opening new Connection -  {}% of maximum connections used.", fill);

                        }
                    });

                    // Waits for the next socket to become available, which may or may not be the result of
                    // the request above.

                    semaphore.acquire();
                    return available.remove();

                }
            } catch (InterruptedException ex) {
                throw new InternalException(ex);
            }
        }

        public void kill(final Connection connection) {
            requests.add(() -> {
                if (connection.context() == shadow) {
                    connection.socket().close();
                    --count;
                } else {
                    logger.error("Invalid Connection Supplied. {} != {}", shadow, connection.context());
                }
            });
        }

        private void run() {

            final int zmax = zContext.getContext().getMaxSockets();

            if (zmax < max) {
                logger.warn("Pool is configured to use {} sockets when ZMQ Context is configured for {} max sockets.", max, zmax);
            }

            final Stopwatch report = Stopwatch.createStarted();
            final Stopwatch stopwatch = Stopwatch.createStarted();

            while (!interrupted()) {
                try {

                    // Always ensures we have the minimum number of Sockets available, and the requests queue
                    // will block for only a second at a time where it will awake and ensure that the minimum
                    // number of connections is upheld.

                    do {

                        // Always adjust the high water mark to ensure we have accurate reporting.

                        highWaterMark.getAndUpdate(hwm -> max(count, hwm));

                        if (report.elapsed(SECONDS) > REPORT_INTERVAL) {

                            final int fill = round( ((float) count / (float) max) * 100f);

                            if (fill > WARNING_PERCENTAGE) {
                                logger.warn("HWM: {} - ({}/{}) {}% of maximum connections used.", getHighWaterMark(), count, max, fill);
                            } else {
                                logger.info("HWM: {} - ({}/{}) {}% of maximum connections used.", getHighWaterMark(), count, max, fill);
                            }

                            report.reset().start();

                        }

                        // Ensures there's a minimum number of connections in the pool.
                        ensureMinimum();

                        // Services all requests
                        final Runnable request = requests.poll(timeout, SECONDS);

                        if (request != null) {
                            // Service any requests to the lifecycle thread and resets the stopwatch back to zero and
                            // so that we stay in this loop for as long as reasonably possible.
                            request.run();
                            stopwatch.reset().start();
                        }

                    } while (stopwatch.elapsed(SECONDS) < timeout);

                    drainConnections();

                    // Resets at the end of the loop so the next iteration will always start with a full timeout
                    // from the beginning.

                    stopwatch.reset();

                } catch (InterruptedException e) {
                    logger.info("Interrupted.  Exiting.");
                    break;
                }

            }

        }

        private void ensureMinimum() {

            // Ensure that before going into the polling loop we always have the minimum number of sockets
            // availble in the pool

            while (count < min) {
                final Connection connection = from(shadow, socketSupplier);
                offer(connection);
                ++count;
            }

        }

        private void drainConnections() {

            // Spin down any unused connections at this point because no activity has happened on this thread
            // for long enough to keep connections open (and therefore tying up resources).  However, we want
            // to err on the side of caution when trying to drain unused connections.  We only drain connections
            // if we can drain them all at once and we know that there must be at least that many availble in the
            // queue.

            for (int toDrain = count - min; toDrain > min; toDrain /= 2) {
                if (semaphore.tryAcquire(toDrain)) {

                    logger.info("Draining {} connections.", toDrain);

                    for (int i = 0; i < toDrain; ++i) {
                        available.remove().close();
                    }

                    count -= toDrain;
                    break;
                }
            }

        }

        @Override
        public void close() {
            shadow.close();
        }

    }

    private static class PooledConnection implements Connection {

        private boolean open = true;

        private final Connection delegate;

        public PooledConnection(final Connection delegate) {
            this.delegate = delegate;
        }

        @Override
        public ZContext context() {
            if (!open) throw new IllegalStateException("Already closed.");
            return delegate.context();
        }

        @Override
        public ZMQ.Socket socket() {
            if (!open) throw new IllegalStateException("Already closed.");
            return delegate.socket();
        }

        @Override
        public void close() {
            logger.error("Attempting to close a pooled Connection.", new RuntimeException());
        }

    }

}
