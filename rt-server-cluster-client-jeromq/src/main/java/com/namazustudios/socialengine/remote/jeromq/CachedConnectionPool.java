package com.namazustudios.socialengine.remote.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import sun.tools.jconsole.Worker;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.MINUTES;

public class CachedConnectionPool implements ConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(CachedConnectionPool.class);

    public static final String TIMEOUT = "com.namazustudios.socialengine.remote.jeromq.CachedConnectionPool.timeout";

    public static final String MIN_CONNECTIONS = "com.namazustudios.socialengine.remote.jeromq.CachedConnectionPool.minConnections";

    private final AtomicReference<Context> context = new AtomicReference<>();

    private ZContext zContext;

    private int timeout;

    private int minConnections;

    @Override
    public void start(final Function<ZContext, ZMQ.Socket> socketSupplier) {

        final Context c = new Context(socketSupplier);

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

        private final AtomicInteger count = new AtomicInteger();

        private final Queue<Connection> connections = new ConcurrentLinkedQueue<>();

        private final AtomicReference<Supplier<Connection>> connectionSupplier = new AtomicReference<>(this::acquire);

        private final ExecutorService executorService = new ThreadPoolExecutor(
            0, Integer.MAX_VALUE,
            getTimeout(), MINUTES, new SynchronousQueue<>(), r -> {
                final Thread thread = new Thread();
                thread.setDaemon(true);
                thread.setName(CachedConnectionPool.class.getSimpleName() + " worker.");
                return thread;
            }, (r, e) -> r.run());

        private final Function<ZContext, ZMQ.Socket> socketSupplier;

        public Context(final Function<ZContext, ZMQ.Socket> socketSupplier) {
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
            connections.forEach(connection -> connection.close());

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
                            logger.warn("Attempting to close managed connection.", new Exception());
                        }

                    });

                    connections.add(connection);

                }  catch (Throwable th) {
                    connection.close();
                    logger.error("Caught error on connection pool.", th);
                    throw th;
                }

            });
        }

        private Connection acquire() {
            final Connection connection = connections.poll();
            return connection == null ? new WorkerConnection() : connection;
        }

        private class WorkerConnection implements Connection, AutoCloseable {

            private final ZMQ.Socket socket = socketSupplier.apply(getzContext());

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
                    logger.error("Caught exception closing Socket.", ex);
                }

                try {
                    getzContext().destroySocket(socket());
                } catch (final Exception ex) {
                    logger.error("Caught exception destroying Socket.", ex);
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

        @Override
        public void close() {}

    }

    private static class TerminatedException extends RuntimeException {
        public TerminatedException() {
            super("Connection pool terminated.");
        }
    }


}
