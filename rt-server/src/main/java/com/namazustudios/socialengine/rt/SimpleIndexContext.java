package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SimpleIndexContext implements IndexContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleResourceContext.class);

    public static final String EXECUTOR_SERVICE = "com.namazustudios.socialengine.rt.SimpleIndexContext.executorService";

    private ExecutorService executorService;

    private ResourceService resourceService;

    @Override
    public Future<Stream<Listing>> listAsync(final Path path,
                                             final Consumer<Stream<Listing>> success,
                                             final Consumer<Throwable> failure) {
        return getExecutorService().submit(() -> {
            try {
                final Stream<Listing> stream = getResourceService().listParallelStream(path).map(this::transform);
                success.accept(stream);
                return getResourceService().listParallelStream(path).map(this::transform);
            } catch (Throwable th) {
                logger.error("Caught exception listing {}", path, th);
                failure.accept(th);
                throw th;
            }
        });
    }

    private Listing transform(final ResourceService.Listing listing) {
        return new Listing() {
            @Override
            public Path getPath() {
                return listing.getPath();
            }

            @Override
            public ResourceId getResourceId() {
                return listing.getResourceId();
            }
        };
    }

    @Override
    public Future<Void> linkAsync(final ResourceId resourceId, final Path destination,
                                  final Consumer<Void> success, final Consumer<Throwable> failure) {
        return getExecutorService().submit(() -> {
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
    public Future<Void> linkPathAsync(final Path source, final Path destination,
                                      final Consumer<Void> success, final Consumer<Throwable> failure) {
        return getExecutorService().submit(() -> {
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
    public Future<Unlink> unlinkAsync(final Path path,
                                      final Consumer<Unlink> success,
                                      final Consumer<Throwable> failure) {
        return getExecutorService().submit(() -> {
            try {
                getResourceService().unlinkPath(path);
                success.accept(null);
                return null;
            } catch (Throwable th) {
                logger.error("Caught error unlinking path {}", path, th);
                failure.accept(th);
                throw th;
            }
        });
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Inject
    public void setExecutorService(@Named(EXECUTOR_SERVICE) ExecutorService executorService) {
        this.executorService = executorService;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

}
