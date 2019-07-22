package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;

public class SimpleRetainedHandlerService implements RetainedHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleRetainedHandlerService.class);

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
        final Path path = Path.fromComponents("tmp", "handler", "re", "*");
        getResourceService().unlinkMultiple(path);
    }

    @Override
    public TaskId perform(
            final Consumer<Object> success, final Consumer<Throwable> failure,
            final long timeout, final TimeUnit timeoutUnit,
            final String module, final Attributes attributes,
            final String method, final Object... args) {

        final Path path = Path.fromComponents("tmp", "handler", "re", randomUUID().toString());
        final Resource resource = acquire(path, module, attributes);
        final ResourceId resourceId = resource.getId();
        getScheduler().scheduleUnlink(path, timeout, timeoutUnit);

        try (final ResourceLockService.Monitor m = getResourceLockService().getMonitor(resourceId)) {

            final AtomicBoolean sent = new AtomicBoolean();
            final AtomicBoolean unlinked = new AtomicBoolean();

            final Runnable unlink = () -> {
                if (unlinked.compareAndSet(false, true)) try {
                    getScheduler().scheduleUnlink(path);
                } catch (ResourceNotFoundException ex) {
                    // It's probably safe to ignore this exception because the resource was removed
                    // intentionally.  We want to make sure there's a way to detect this, but it's
                    // not likely necessary.
                    logger.trace("Caught exception un-linking Resource {}", resourceId, ex);
                } catch (Exception ex) {
                    logger.error("Caught exception un-linking Resource {}", resourceId, ex);
                }
            };

            final Consumer<Throwable> _failure = t -> {
                try {

                    final String _args = stream(args)
                        .map(a -> a == null ? "null" : a.toString())
                        .collect(Collectors.joining(","));

                    logger.error("Caught exception processing retained handler {}.{}({}).", module, method, _args, t);
                    if (sent.compareAndSet(false, true)) failure.accept(t);

                } catch (Exception ex) {
                    logger.error("Caught exception destroying resource {}", resourceId, ex);
                } finally {
                    unlink.run();
                }
            };

            final Consumer<Object> _success = o -> {
                try {
                    if (sent.compareAndSet(false, true)) success.accept(o);
                } catch (Throwable th) {
                    _failure.accept(th);
                } finally {
                    unlink.run();
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
