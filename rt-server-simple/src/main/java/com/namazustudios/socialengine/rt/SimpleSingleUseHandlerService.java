package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;

public class SimpleSingleUseHandlerService implements SingleUseHandlerService {

    private static final int PURGE_BATCH_SIZE = 100;

    private static final Logger logger = LoggerFactory.getLogger(SimpleSingleUseHandlerService.class);

    private Scheduler scheduler;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private ResourceLockService resourceLockService;

    private final AtomicBoolean running = new AtomicBoolean();

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            purge();
        } else {
            throw new IllegalStateException("Already started.");
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            purge();
        } else {
            throw new IllegalStateException("Already started.");
        }
    }

    private void purge() {

        final Path path = Path.fromComponents("tmp", "handler", "su", "*");

        List<ResourceService.Unlink> unlinkList;

        do {
            unlinkList = getResourceService().unlinkMultiple(path, PURGE_BATCH_SIZE);
            logger.info("Purged {} resources.", unlinkList.size());
            logger.debug("Purged [{}]", unlinkList);
        } while (!unlinkList.isEmpty());

    }

    @Override
    public TaskId perform(final Consumer<Object> success, final Consumer<Throwable> failure,
                          long timeoutDelay, TimeUnit timeoutUnit,
                          final String module, final Attributes attributes,
                          final String method, final Object... args) {
        final Path path = Path.fromComponents("tmp", "handler", "su", randomUUID().toString());
        final Resource resource = acquire(path, module, attributes);
        final ResourceId resourceId = resource.getId();
        final Future<Void> destructionFuture = getScheduler().scheduleDestruction(resourceId, timeoutDelay, timeoutUnit);

        try (final ResourceLockService.Monitor m = getResourceLockService().getMonitor(resourceId)) {

            final AtomicBoolean sent = new AtomicBoolean();
            final AtomicBoolean destroyed = new AtomicBoolean();

            final Runnable destroy = () -> {
                if (destroyed.compareAndSet(false, true)) try {
                    getScheduler().scheduleDestruction(resourceId);
                } catch (Exception ex) {
                    logger.error("Error scheduling destruction for resource {}", resourceId, ex);
                } finally {
                    destructionFuture.cancel(false);
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
                    logger.error("Caught exception sending response from resource {}", resourceId, ex);
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
