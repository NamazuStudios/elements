package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.id.TaskId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.joining;

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

            final AtomicBoolean sent = new AtomicBoolean();
            final AtomicBoolean destroyed = new AtomicBoolean();

            final Runnable destroy = () -> {
                if (destroyed.compareAndSet(false, true)) try {
                    getScheduler().scheduleDestruction(resourceId);
                } catch (Exception ex) {
                    logger.error("Error scheudling destruction for resource {}", resourceId, ex);
                }
            };

            final Consumer<Throwable> _failure = t -> {
                try {

                    final String _args = stream(args)
                        .map(a -> a == null ? "null" : a.toString())
                        .collect(Collectors.joining(","));

                    logger.error("Caught exception processing single-use handler {}.{}({}).", module, method, _args, t);
                    if (sent.compareAndSet(false, true)) failure.accept(t);

                } catch (Exception ex) {
                    logger.error("Caught exception destroying resource {}", resourceId, ex);
                } finally {
                    destroy.run();
                }
            };

            final Consumer<Object> _success = o -> {
                try {
                    if (sent.compareAndSet(false, true)) success.accept(o);
                } catch (Throwable th) {
                    _failure.accept(th);
                } finally {
                    destroy.run();
                }
            };

            return resource
                .getMethodDispatcher(method)
                .params(args)
                .dispatch(_success, _failure);

        } finally {
            getResourceService().tryRelease(resource);
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
