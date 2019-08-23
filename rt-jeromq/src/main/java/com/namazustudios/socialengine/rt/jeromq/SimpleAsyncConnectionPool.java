package com.namazustudios.socialengine.rt.jeromq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleAsyncConnectionPool implements AsyncConnectionPool {

    private static final Logger logger = LoggerFactory.getLogger(SimpleAsyncConnectionPool.class);

    private int minConnections;

    private int maxConnextions;

    private AsyncConnectionService asyncSocketService;

    private final AtomicReference<SimpleAsyncConnectionPoolContext> context = new AtomicReference<>();

    @Override
    public void start(final Function<ZContext, ZMQ.Socket> socketSupplier, final String name) {

        final SimpleAsyncConnectionPoolContext context = new SimpleAsyncConnectionPoolContext(name, socketSupplier);

        if (this.context.compareAndSet(null, context)) {
            logger.info("Starting up.");
            context.start();
            logger.info("Started up.");
        } else {
            throw new IllegalStateException("Already running.");
        }

    }

    @Override
    public void stop() {

        final SimpleAsyncConnectionPoolContext context = this.context.getAndSet(null);

        if (context == null) {
            throw new IllegalStateException("Already running.");
        } else {
            logger.info("Shutting down.");
            context.stop();
            logger.info("Shut down.");
        }

    }

    @Override
    public void acquireNextAvailableConnection(final Consumer<AsyncConnection> asyncConnectionConsumer) {
        final SimpleAsyncConnectionPoolContext context = getContext();
        context.acquireNextAvailableConnection(asyncConnectionConsumer);
    }

    public int getMinConnections() {
        return minConnections;
    }

    @Inject
    public void setMinConnections(@Named(MIN_CONNECTIONS) int minConnections) {
        this.minConnections = minConnections;
    }

    public int getMaxConnextions() {
        return maxConnextions;
    }

    @Inject
    public void setMaxConnextions(@Named(MAX_CONNECTIONS) int maxConnextions) {
        this.maxConnextions = maxConnextions;
    }

    public AsyncConnectionService getAsyncSocketService() {
        return asyncSocketService;
    }

    @Inject
    public void setAsyncSocketService(AsyncConnectionService asyncSocketService) {
        this.asyncSocketService = asyncSocketService;
    }

    private SimpleAsyncConnectionPoolContext getContext() {
        final SimpleAsyncConnectionPoolContext context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    private class SimpleAsyncConnectionPoolContext {

        private final String name;

        private AsyncConnectionService.ManagedPool managedPool;

        private final Function<ZContext, ZMQ.Socket> socketSupplier;

        public SimpleAsyncConnectionPoolContext(final String name,
                                                final Function<ZContext, ZMQ.Socket> socketSupplier) {
            this.name = name;
            this.socketSupplier = socketSupplier;
        }

        public void start() {
            managedPool = getAsyncSocketService().allocatePool(
                name,
                getMinConnections(), getMaxConnextions(),
                socketSupplier);
        }

        public void stop() {
            managedPool.close();
        }

        public void acquireNextAvailableConnection(final Consumer<AsyncConnection> asyncConnectionConsumer) {
            managedPool.acquireNextAvailableConnection(asyncConnectionConsumer);
        }

    }

}
