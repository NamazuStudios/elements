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

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class DynamicConnectionPool implements ConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(DynamicConnectionPool.class);

    public static final String TIMEOUT = "com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.timeout";

    public static final String MIN_CONNECTIONS = "com.namazustudios.socialengine.rt.jeromq.DynamicConnectionPool.minConnections";

    private final AtomicInteger highWaterMark = new AtomicInteger();

    private final AtomicReference<Context> context = new AtomicReference<>();

    private ZContext zContext;

    private int timeout;

    private int minConnections;

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

    public ZContext getzContext() {
        return zContext;
    }

    @Inject
    public void setzContext(ZContext zContext) {
        this.zContext = zContext;
    }

    private class Context {

        private final String name;

        private final AtomicReference<Supplier<Connection>> connectionSupplier = new AtomicReference<>(WorkerConnection::new);

        private final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

        private final ThreadPoolExecutor executorService;
        {

            final AtomicInteger count = new AtomicInteger();

            executorService = new ThreadPoolExecutor(
                    0, Integer.MAX_VALUE,
                    getTimeout(), SECONDS, new SynchronousQueue<>(),
                    r -> {

                        final Thread thread = new Thread(() -> {

                            final Connection connection = connectionSupplier.get().get();
                            connectionThreadLocal.set(connection);

                            try {
                                r.run();
                            } catch (Throwable th) {
                                connectionThreadLocal.set(null);
                                logger.error("{} Caught error on connection pool.", toString(), th);
                                connection.close();
                                throw th;
                            }

                        });

                        thread.setDaemon(true);
                        thread.setName(toString() + " worker #" + count.getAndIncrement());
                        return thread;
                    }, (r, e) -> {
                try {
                    r.run();
                } catch (TerminatedException tex) {
                    logger.debug("{} Connection terminated.", toString());
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
            connectionSupplier.set(TerminalConnection::new);

            try {
                executorService.awaitTermination(2, MINUTES);
            } catch (InterruptedException ex) {
                throw new InternalError("Interrupted while shutting down connection pool.", ex);
            }

        }

        public <T> Future<T> process(final Function<Connection, T> connectionTFunction) {

            final FutureTask<T> futureTask = new FutureTask<T>(() -> {

                final Connection connection = connectionThreadLocal.get();

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
                } catch (Throwable th) {
                    connection.close();
                    logger.error("{} Caught error on connection pool.", toString(), th);
                    throw th;
                }

            });

            executorService.submit(futureTask);
            return futureTask;

        }

        @Override
        public String toString() {
            return "Context{" +
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

            }

        }

    }

    private static class TerminalConnection  implements Connection {

        @Override
        public ZContext context() { throw new TerminatedException(); }

        @Override
        public ZMQ.Socket socket() { throw new TerminatedException(); }

        @Override
        public void close() { throw new TerminatedException(); }

    }

    private static class TerminatedException extends RuntimeException {
        public TerminatedException() {
            super("Connection pool terminated.");
        }
    }

}
