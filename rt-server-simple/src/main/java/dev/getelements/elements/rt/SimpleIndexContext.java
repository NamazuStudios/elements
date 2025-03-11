package dev.getelements.elements.rt;

import dev.getelements.elements.rt.annotation.RemotelyInvokable;
import dev.getelements.elements.rt.annotation.Routing;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.remote.Instance;
import dev.getelements.elements.rt.remote.provider.ExecutorServiceFactory;
import dev.getelements.elements.rt.routing.ListAggregateRoutingStrategy;
import dev.getelements.elements.sdk.util.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

public class SimpleIndexContext implements IndexContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleResourceContext.class);

    private ExecutorService executorService;

    private ResourceService resourceService;

    private final Lock lock = new ReentrantLock();

    private volatile ExecutorServiceFactory<ExecutorService> executorServiceFactory;

    @Override
    public void start() {
        try (var monitor = Monitor.enter(lock)) {
            if (executorService == null) {
                executorService = getExecutorServiceFactory().getService(SimpleIndexContext.class);
            } else {
                throw new IllegalStateException("Already started.");
            }
        }
    }

    @Override
    public void stop() {
        try (var monitor = Monitor.enter(lock)) {
            if (executorService == null) {
                throw new IllegalStateException("Not running.");
            } else {

                executorService.shutdown();

                try {
                    if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                        logger.error("Timed out.");
                    }
                } catch (InterruptedException ex) {
                    logger.error("Interrupted");
                } finally {
                    executorService = null;
                }

            }
        }
    }

    @RemotelyInvokable(routing = @Routing(ListAggregateRoutingStrategy.class))
    @Override
    public void listAsync(final Path path,
                          final Consumer<List<Listing>> success,
                          final Consumer<Throwable> failure) {
        getExecutorService().submit(() -> {
            try {

                final List<Listing> listings = getResourceService()
                    .listStream(path)
                    .map(SimpleIndexContextListing::new)
                    .collect(toList());

                success.accept(listings);

            } catch (Throwable th) {
                logger.error("Caught exception listing {}", path, th);
                failure.accept(th);
                throw th;
            }
        });
    }

    @Override
    public void linkAsync(final ResourceId resourceId, final Path destination,
                           final Consumer<Void> success, final Consumer<Throwable> failure) {
        getExecutorService().submit(() -> {
            try {
                getResourceService().link(resourceId, destination);
                success.accept(null);
                return null;
            } catch (Throwable th) {
                logger.error("Caught exception processing link {} -> {}", resourceId, destination, th);
                failure.accept(th);
                throw th;
            }
        });
    }

    @Override
    public void linkPathAsync(final Path source, final Path destination,
                              final Consumer<Void> success, final Consumer<Throwable> failure) {
        getExecutorService().submit(() -> {
            try {
                getResourceService().linkPath(source, destination);
                success.accept(null);
                return null;
            } catch (Throwable th) {
                logger.error("Caught exception processing link {} -> {}", source, destination, th);
                failure.accept(th);
                throw th;
            }
        });
    }

    @Override
    public void unlinkAsync(final Path path,
                            final Consumer<Unlink> success,
                            final Consumer<Throwable> failure) {
        getExecutorService().submit(() -> {
            try {
                final ResourceService.Unlink delegate = getResourceService().unlinkPath(path);
                final SimpleIndexContextUnlink result = new SimpleIndexContextUnlink(delegate);
                success.accept(result);
                return result;
            } catch (Throwable th) {
                logger.error("Caught error unlinking path {}", path, th);
                failure.accept(th);
                throw th;
            }
        });
    }

    public ExecutorService getExecutorService() {
        if (executorService == null) throw new IllegalStateException("Not running.");
        return executorService;
    }

    public ExecutorServiceFactory<ExecutorService> getExecutorServiceFactory() {
        return executorServiceFactory;
    }

    @Inject
    public void setExecutorServiceFactory(@Named(Instance.EXECUTOR_SERVICE) ExecutorServiceFactory<ExecutorService> executorServiceFactory) {
        this.executorServiceFactory = executorServiceFactory;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public static class SimpleIndexContextUnlink implements Unlink, Serializable {

        private final boolean destroyed;

        private final ResourceId resourceId;

        public SimpleIndexContextUnlink(final ResourceService.Unlink unlink) {
            destroyed = unlink.isRemoved();
            resourceId = unlink.getResourceId();
        }

        @Override
        public boolean isDestroyed() {
            return destroyed;
        }

        @Override
        public ResourceId getResourceId() {
            return resourceId;
        }

    }
}
