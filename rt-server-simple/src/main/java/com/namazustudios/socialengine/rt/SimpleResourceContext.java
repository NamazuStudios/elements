package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.id.TaskId;
import com.namazustudios.socialengine.rt.remote.SimpleWorkerInstance;
import com.namazustudios.socialengine.rt.util.LatchedExecutorServiceCompletionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;


public class SimpleResourceContext implements ResourceContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleResourceContext.class);

    private Scheduler scheduler;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private ExecutorService executorService;

    private final AtomicReference<LatchedExecutorServiceCompletionService> completionService = new AtomicReference<>();

    @Override
    public void start() {

        final var service = new LatchedExecutorServiceCompletionService(getExecutorService());

        if (completionService.compareAndSet(null, service)) {

            logger.info("Starting.");

            try {
                getResourceService().start();
            } catch (Exception ex) {
                completionService.compareAndSet(service, null);
                throw ex;
            }

            logger.info("Started.");

        } else {
            throw new IllegalStateException("Already running.");
        }

    }

    @Override
    public void stop() {
        final var completionService = this.completionService.getAndSet(null);

        if (completionService == null) {
            throw new IllegalStateException("Not running.");
        } else {

            try {
                completionService.stop();
            } catch (Exception ex) {
                logger.error("Caught exception stopping completion service.", ex);
            }

            try {
                getResourceService().stop();
            } catch (Exception ex) {
                logger.error("Caught exception stoppping resource service.", ex);
            }

        }

    }

    private LatchedExecutorServiceCompletionService getCompletionService() {
        final LatchedExecutorServiceCompletionService completionService = this.completionService.get();
        if (completionService == null) throw new IllegalStateException("Already running.");
        return completionService;
    }

    @Override
    public ResourceId createAttributes(final String module, final Path path,
                                       final Attributes attributes, final Object... args) {
        logger.debug("Loading module {} -> {}", module, path);
        final Resource resource = getResourceLoader().load(module, attributes, args);
        getResourceService().addAndAcquireResource(path, resource);
        return resource.getId();
    }

    @Override
    public void createAttributesAsync(final Consumer<ResourceId> success, final Consumer<Throwable> failure,
                                      final String module, final Path path, final Attributes attributes, final Object... args) {
        getCompletionService().submit(() -> {
            try {
                final ResourceId resourceId = createAttributes(module, path, attributes, args);
                success.accept(resourceId);
                return resourceId;
            } catch (Throwable th) {
                logger.error("Caught Exception loading module {} -> {}", module, path, th);
                failure.accept(th);
                throw th;
            }
        });
    }

    public void destroyAsync(final Consumer<Void> success,
                             final Consumer<Throwable> failure,
                             final ResourceId resourceId) {
        // The Resource must be locked in order to properly destroy it because it invovles mutating the Resource itself.
        // if we try to destroy it without using the scheduler, we could end up with two threads accessing it at the
        // same time, which is no good.
        getScheduler().performV(resourceId, r -> {
            try {
                getResourceService().destroy(resourceId);
                success.accept(null);
            } catch (Throwable throwable) {
                failure.accept(throwable);
            }
        }, failure.andThen(th -> logger.error("Failure", th)));
    }

    @Override
    public void invokeAsync(final Consumer<Object> success, final Consumer<Throwable> failure,
                            final ResourceId resourceId, final String method, final Object... args) {
        getScheduler().perform(resourceId, resource -> doInvoke(success, failure, resource, method, args), failure);
    }

    @Override
    public void invokePathAsync(final Consumer<Object> success, final Consumer<Throwable> failure,
                                final Path path, final String method, final Object... args) {
        getScheduler().perform(path, resource -> doInvoke(success, failure, resource, method, args), failure);
    }

    private TaskId doInvoke(final Consumer<Object> success, final Consumer<Throwable> failure,
                            final Resource resource, final String method, final Object... args) {
        try {
            return resource.getMethodDispatcher(method).params(args).dispatch(success, failure);
        } catch (Throwable th) {
            failure.accept(th);
            throw th;
        }
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Inject
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Inject
    public void setExecutorService(@Named(SimpleWorkerInstance.EXECUTOR_SERVICE) ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void destroyAllResources() {
        getResourceService().removeAndCloseAllResources();
    }

    @Override
    public void destroyAllResourcesAsync(Consumer<Void> success, Consumer<Throwable> failure) {
        getCompletionService().submit(() -> {

            try {
                getResourceService().removeAndCloseAllResources();
                success.accept(null);
            } catch (Throwable th) {
                failure.accept(th);
                throw th;
            }

            return null;
        });
    }

}
