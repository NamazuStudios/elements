package com.namazustudios.socialengine.rt.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class DynamicConnectionPool implements ConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(DynamicConnectionPool.class);

    public static final String TIMEOUT = "com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.timeout";

    public static final String MIN_CONNECTIONS = "com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.minConnections";

    public static final String MAX_CONNECTIONS = "com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.maxConnections";

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
            c.start();
        } else {
            throw new IllegalStateException("Already started.");
        }

    }

    @Override
    public void stop() {

        final Context c = context.get();

        if (context.compareAndSet(c, null)) {
            c.stop();
        } else {
            throw new IllegalStateException("Not started");
        }

    }

    @Override
    public <T> Future<T> process(final Function<Connection, T> consumer) {

        final Context c = context.get();

        if (c != null) {
            return c.process(consumer);
        } else {
            throw new IllegalStateException("Not started.");
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

    public int getMaxConnections() {
        return maxConnections;
    }

    @Inject
    public void setMaxConnections(@Named(MAX_CONNECTIONS) int maxConnections) {
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

        private final String name;

        private final ConcurrentMap<Thread, Connection> connectionMap = new ConcurrentHashMap<>();

        private final AtomicReference<Supplier<Connection>> connectionSupplier = new AtomicReference<>(WorkerConnection::new);

        private final ThreadPoolExecutor executorService;
        {

            final AtomicInteger count = new AtomicInteger();

            executorService = new ThreadPoolExecutor(
                0, getMaxConnections(),
                getTimeout(), SECONDS, new SynchronousQueue<>(),
                r -> {
                    final Thread thread = new Thread(() -> {
                        try {
                            r.run();
                        } finally {
                            final Connection connection = connectionMap.get(currentThread());
                            if (connection != null) connection.close();
                        }
                    });
                    thread.setDaemon(true);
                    thread.setName(toString() + " worker #" + count.getAndIncrement());
                    return thread;
                },
                (r, e) -> {

                    Connection existing = connectionMap.put(currentThread(), new TerminalConnection(ExhaustedException::new));

                    try {
                        // If it's rejected, then we allow the code to run, however, we perform it on the current thread
                        // with a specific type of Connection which will throw an exception indicating so.  This only
                        // happens when the
                        r.run();
                    } catch (TerminatedException ex) {
                        logger.debug("{} pool terminated.", toString(), ex);
                    } finally {
                        if (existing != null) existing.close();
                        existing = connectionMap.remove(currentThread());
                        if (existing != null) existing.close();
                    }

              });
        }

        private final Function<ZContext, ZMQ.Socket> socketSupplier;

        public Context(final Function<ZContext, ZMQ.Socket> socketSupplier, final String name) {
            this.name = name;
            this.socketSupplier = socketSupplier;
        }

        public void start() {
            executorService.setCorePoolSize(getMinConnections());
        }

        public void stop() {

            executorService.shutdownNow();
            connectionSupplier.set(() -> new TerminalConnection(TerminatedException::new));

            try {
                executorService.awaitTermination(2, MINUTES);
            } catch (InterruptedException ex) {
                throw new InternalError("Interrupted while shutting down connection pool.", ex);
            }

            if (!connectionMap.isEmpty()) {
                logger.warn("Not all connections closed. {} lingering connections exist.", connectionMap.size());
            }

        }

        public <T> Future<T> process(final Function<Connection, T> connectionTFunction) {

            final FutureTask<T> futureTask = new FutureTask<T>(() -> {

                final Connection connection;
                connection = connectionMap.computeIfAbsent(currentThread(), thread -> connectionSupplier.get().get());

                try {
                    return connectionTFunction.apply(new Connection() {

                        @Override
                        public ZContext context() {
                            return connection.context();
                        }

                        @Override
                        public ZMQ.Socket socket() {
                            return connection.socket();
                        }

                        @Override
                        public void close() {
                            logger.warn("{} Attempting to close managed connection.", toString(), new UnsupportedOperationException());
                        }

                    });
                } catch (ExpectedException ex) {
                    final Throwable cause = ex.getCause();
                    if (cause instanceof Exception) {
                        throw (Exception) cause;
                    } else {
                        throw ex;
                    }
                } catch (TerminatedException ex) {
                    logger.info("{} Connection terminated.  Failing gracefully.", toString(), ex);

                    // This is an expected exception type, so we dont' need to close the connection because it would
                    // have been thrown from the connection itself which we have determined is dead.

                    final Connection removed = connectionMap.remove(currentThread());

                    if (removed != connection) {
                        removed.close();
                        logger.warn("Detected connection mismatch {} != {}", removed, connection);
                    }

                    throw ex;

                } catch (Exception ex) {

                    // This must close out the connection because the underlying socket is now in an undefined state as
                    // a future operation may end up with an unexpected response from a previous operation that did not
                    // complete properly.

                    logger.error("{} Caught error on connection pool.", toString(), ex);
                    final Connection removed = connectionMap.remove(currentThread());

                    if (removed != connection) {
                        removed.close();
                        logger.warn("Detected connection mismatch {} != {}", removed, connection);
                    }

                    connection.close();
                    throw ex;

                }

            });

            executorService.submit(futureTask);
            return futureTask;

        }

        @Override
        public String toString() {
            return "DynamicConnectionPool{" +
                    "name='" + name + '\'' +
                    '}';
        }

        private class WorkerConnection implements Connection, AutoCloseable {

            private final ZMQ.Socket socket = socketSupplier.apply(getzContext());

            public WorkerConnection() {
                highWaterMark.incrementAndGet();
            }

            @Override
            public ZContext context() {
                return getzContext();
            }

            @Override
            public ZMQ.Socket socket() {
                return socket;
            }

            @Override
            public void close() {

                try {
                    socket().close();
                } catch (final Exception ex) {
                    logger.error("{} Caught exception closing Socket.", toString(), ex);
                }

                try {
                    getzContext().destroySocket(socket());
                } catch (final Exception ex) {
                    logger.error("{} Caught exception destroying Socket.", toString(), ex);
                }

                logger.info("Successfully closed socket {} ", socket);

            }

        }

    }

    private static class TerminalConnection  implements Connection {

        private final Supplier<RuntimeException> exceptionSupplier;

        public TerminalConnection(Supplier<RuntimeException> exceptionSupplier) {
            this.exceptionSupplier = exceptionSupplier;
        }

        @Override
        public ZContext context() { throw exceptionSupplier.get(); }

        @Override
        public ZMQ.Socket socket() { throw exceptionSupplier.get(); }

        @Override
        public void close() {}

    }

    private static class ExhaustedException extends RuntimeException {
        public ExhaustedException() {
            super("Connection pool exhausted.");
        }
    }

    private static class TerminatedException extends RuntimeException {
        public TerminatedException() {
            super("Connection pool terminated.");
        }
    }

}
