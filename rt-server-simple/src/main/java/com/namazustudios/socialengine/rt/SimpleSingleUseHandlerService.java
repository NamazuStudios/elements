package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.util.UUID.randomUUID;

public class SimpleSingleUseHandlerService implements SingleUseHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleSingleUseHandlerService.class);

    private Scheduler scheduler;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private ResourceLockService resourceLockService;

    private Queue<ResourceId> resourceIdList;

    @Override
    public void start() {
        resourceIdList = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void stop() {
        resourceIdList.forEach(rid -> getResourceService().destroy(rid));
        resourceIdList = null;
    }

    @Override
    public TaskId perform(final Consumer<Object> success, final Consumer<Throwable> failure,
                        final String module, final Attributes attributes,
                        final String method, final Object... args) {
        final Path path = Path.fromComponents("tmp", "handler", "su", randomUUID().toString());
        final Resource resource = acquire(path, module, attributes);
        final ResourceId resourceId = resource.getId();

        try (final ResourceLockService.Monitor m = getResourceLockService().getMonitor(resourceId)) {

            final AtomicBoolean processed = new AtomicBoolean();

            final Runnable destroy = () -> {
                if (processed.compareAndSet(false, true)) try {
                    getScheduler().scheduleDestruction(resourceId);
                } catch (Exception ex) {
                    logger.error("Error scheudling destruction for resource {}", resourceId, ex);
                }
            };

            final Consumer<Throwable> _failure = t -> {
                try {
                    destroy.run();
                } catch (Exception ex) {
                    logger.error("Caught exception destroying resource {}", resourceId, ex);
                }
            };

            final Consumer<Object> _success = o -> {
                try {
                    destroy.run();
                } catch (Throwable th) {
                    _failure.accept(th);
                }
            };

            return resource
                .getMethodDispatcher(method)
                .params(args)
                .dispatch(_success.andThen(success), _failure.andThen(failure));

        } finally {
            getResourceService().release(resource);
        }

    }

    private Resource acquire(final Path path, final String module, final Attributes attributes) {
        final Resource resource = getResourceLoader().load(module, attributes);
        return getResourceService().addAndAcquireResource(path, resource);
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

    public ResourceLockService getResourceLockService() {
        return resourceLockService;
    }

    @Inject
    public void setResourceLockService(ResourceLockService resourceLockService) {
        this.resourceLockService = resourceLockService;
    }

}
