package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;


public class SimpleResourceContext implements ResourceContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleResourceContext.class);

    public static final String EXECUTOR_SERVICE = "com.namazustudios.socialengine.rt.SimpleResourceContext.executorService";

    private Scheduler scheduler;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private ExecutorService executorService;

    @Override
    public ResourceId create(final Path path, final String module, final Object ... args) {
        logger.info("Loading module {} -> {}", module, path);
        final Resource resource = getResourceLoader().load(module, args);
        getResourceService().addResource(path, resource);
        return resource.getId();
    }

    @Override
    public Future<ResourceId> createAsync(final Consumer<ResourceId> success, final Consumer<Throwable> failure,
                                          final Path path, final String module, final Object... args) {
        return getExecutorService().submit(() -> {
            try {
                final ResourceId resourceId = create(path, module, args);
                success.accept(resourceId);
                return resourceId;
            } catch (Throwable th) {
                logger.error("Caught Exception loading module {} -> {}", module, path, th);
                failure.accept(th);
                throw th;
            }
        });
    }

    public Future<Void> destroyAsync(final ResourceId resourceId, Consumer<Void> success, Consumer<Throwable> failure) {
        return getScheduler().performV(resourceId, r -> getResourceService().destroy(resourceId));
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
    public void setExecutorService(@Named(EXECUTOR_SERVICE) ExecutorService executorService) {
        this.executorService = executorService;
    }

}
