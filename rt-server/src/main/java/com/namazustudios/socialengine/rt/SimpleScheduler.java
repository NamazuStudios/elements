package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The simple handler server is responsible for dispatching requests and events to all {@link Resource} instances
 * contained therein.  It accomplishes its task in parallel by dispatching all requests, events, and then
 * finally updating each {@link Resource} in order.
 *
 * Internally, it leverages an instance an {@link ExecutorService} and a {@link CompletionService} to
 * perform all updates in parallel.
 *
 * Created by patricktwohig on 8/22/15.
 */
public class SimpleScheduler implements Scheduler {

    private static final Logger logger = LoggerFactory.getLogger(SimpleScheduler.class);

    /**
     * The SimpleScheduler uses an {@link ExecutorService} to process requests and dispatch
     * events to the various {@link Resource}s.  This names the specific {@link ExecutorService}
     * to use for injectiong using {@link Named}
     */
    public static final String SCHEDULED_EXECUTOR_SERVICE = "com.namazustudios.socialengine.rt.SimpleScheduler.scheduledExecutorService";

    private ResourceLockService resourceLockService;

    private ResourceService resourceService;

    private ScheduledExecutorService scheduledExecutorService;

    @Override
    public void shutdown() {}

    @Override
    public <T> Future<T> perform(final ResourceId resourceId,
                                 final Function<Resource, T> operation,
                                 final Consumer<Throwable> failure) {
        return getScheduledExecutorService().submit(protectedCallable(resourceId, operation, failure));
    }

    @Override
    public <T> Future<T> perform(final Path path,
                                 final Function<Resource, T> operation,
                                 final Consumer<Throwable> failure) {
        return getScheduledExecutorService().submit(protectedCallable(path, operation, failure));
    }

    @Override
    public <T> Future<T> performAfterDelay(final ResourceId resourceId,
                                           final long time, final TimeUnit timeUnit,
                                           final Function<Resource, T> operation,
                                           final Consumer<Throwable> failure) {
        return getScheduledExecutorService().schedule(protectedCallable(resourceId, operation, failure), time, timeUnit);
    }

    private <T> Callable<T> protectedCallable(final ResourceId resourceId,
                                              final Function<Resource, T> operation,
                                              final Consumer<Throwable> failure) {
        return () -> {
            try {
                final Resource resource = getResourceService().getResourceWithId(resourceId);
                return performProtected(resource, operation);
            } catch (Throwable th) {
                failure.accept(th);
                throw th;
            }
        };
    }

    private <T> Callable<T> protectedCallable(final Path path,
                                              final Function<Resource, T> operation,
                                              final Consumer<Throwable> failure) {
        return () -> {
            try {
                final Resource resource = getResourceService().getResourceAtPath(path);
                return performProtected(resource, operation);
            } catch (Throwable th) {
                failure.accept(th);
                throw th;
            }
        };
    }

    private <T> T performProtected(final Resource resource,
                                   final Function<Resource, T> operation) {

        final Lock lock = getResourceLockService().getLock(resource.getId());

        try {
            logger.trace("Locking resource {}", resource.getId());
            lock.lock();
            logger.trace("Applying operation for resource {}", resource.getId());
            return operation.apply(resource);
        } catch (Throwable th) {
            logger.error("Caught exception in protected operation {}", operation, th);
            throw th;
        } finally {
            logger.trace("Unlocking resource {}", resource.getId());
            lock.unlock();
            logger.trace("Unlocked resource {}", resource.getId());
        }

    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    @Inject
    public void setScheduledExecutorService(@Named(SCHEDULED_EXECUTOR_SERVICE) ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public ResourceLockService getResourceLockService() {
        return resourceLockService;
    }

    @Inject
    public void setResourceLockService(ResourceLockService resourceLockService) {
        this.resourceLockService = resourceLockService;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

}
