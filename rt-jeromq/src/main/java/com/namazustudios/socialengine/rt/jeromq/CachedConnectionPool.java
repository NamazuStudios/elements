package com.namazustudios.socialengine.rt.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class CachedConnectionPool implements ConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(CachedConnectionPool.class);

    public static final String TIMEOUT = "com.namazustudios.socialengine.rt.jeromq.CachedConnectionPool.timeout";

    public static final String MIN_CONNECTIONS = "com.namazustudios.socialengine.rt.jeromq.CachedConnectionPool.minConnections";

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
    public void process(final Consumer<Connection> consumer) {

        final Context c = context.get();

        if (c != null) {
            c.process(consumer);
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

        private final Queue<Connection> connections = new ConcurrentLinkedQueue<>();

        private final AtomicReference<Supplier<Connection>> connectionSupplier = new AtomicReference<>(this::acquire);

        private final ExecutorService executorService = new ThreadPoolExecutor(
                0, Integer.MAX_VALUE,
                getTimeout(), SECONDS, new SynchronousQueue<>(),
        r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(false);
            thread.setName(toString() + " worker.");
            return thread;
        }, (r, e) ->  {
            try {
                r.run();
            } catch (TerminatedException tex) {
                logger.debug("{} Connection terminated.", toString());
            }
        });

        private final Function<ZContext, ZMQ.Socket> socketSupplier;

        public Context(final Function<ZContext, ZMQ.Socket> socketSupplier, final String name) {
            this.name = name;
            this.socketSupplier= socketSupplier;
        }

        public void start() {

            final int numConnections = getMinConnections();

            for (int i = 0; i < numConnections; ++i) {
                final WorkerConnection workerConnection = new WorkerConnection();
                connections.add(workerConnection);
            }

        }

        public void stop() {

            executorService.shutdownNow();
            connectionSupplier.set(TerminalConnection::new);
            xions.forEach(connection -> connection.close());

            try {
                executorService.awaitTermination(5, MINUTES);
            } catch (InterruptedException ex) {
                throw new InternalError("Interrupted while shutting down connection pool.", ex);
            }

        }

        public void process(final Consumer<Connection> connectionConsumer) {
            executorService.submit(() -> {

                final Connection connection = connectionSupplier.get().get();

                try {

                    connectionConsumer.accept(new Connection() {

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
                            logger.warn("{} Attempting to close managed connection.", toString(), new Exception());
                        }

                    });

                    logger.info("{} recycling connection.", toString());
                    connections.add(connection);

                }  catch (Throwable th) {
                    connection.close();
                    logger.error("{} Caught error on connection pool.", toString(), th);
                    throw th;
                }

            });
        }

        private Connection acquire() {
            final Connection connection = connections.poll();
            return connection == null ? new WorkerConnection() : connection;
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
