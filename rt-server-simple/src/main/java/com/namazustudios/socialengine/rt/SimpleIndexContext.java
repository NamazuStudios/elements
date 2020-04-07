package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;
import com.namazustudios.socialengine.rt.annotation.Routing;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.routing.ListAggregateRoutingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static java.util.stream.Collectors.toList;

public class SimpleIndexContext implements IndexContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleResourceContext.class);

    public static final String EXECUTOR_SERVICE = "com.namazustudios.socialengine.rt.SimpleIndexContext.executorService";

    private ExecutorService executorService;

    private ResourceService resourceService;

    @RemotelyInvokable(routing = @Routing(ListAggregateRoutingStrategy.class))
    @Override
    public void listAsync(final Path path,
                          final Consumer<List<Listing>> success,
                          final Consumer<Throwable> failure) {
        getExecutorService().submit(() -> {
            try {

                final List<Listing> listings = getResourceService()
                    .listParallelStream(path)
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

                final Unlink result = new Unlink() {

                    final ResourceService.Unlink delegate = getResourceService().unlinkPath(path);

                    @Override
                    public ResourceId getResourceId() {
                        return delegate.getResourceId();
                    }

                    @Override
                    public boolean isDestroyed() {
                        return delegate.isRemoved();
                    }

                };

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
