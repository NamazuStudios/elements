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
 * contained therein.
 *
 * Internally, it leverages an instance an {@link ExecutorService} and a {@link CompletionService} to
 * perform all updates in parallel.
 *
 * Created by patricktwohig on 8/22/15.
 */
public class SimpleScheduler implements Scheduler {

    private static final Logger logger = LoggerFactory.getLogger(SimpleScheduler.class);

    public static final String SCHEDULED_EXECUTOR_SERVICE = "com.namazustudios.socialengine.rt.SimpleScheduler.scheduledExecutorService";

    public static final String DISPATCHER_EXECUTOR_SERVICE = "com.namazustudios.socialengine.rt.SimpleScheduler.dispatcherExecutorService";

    private ResourceLockService resourceLockService;

    private ResourceService resourceService;

    private ExecutorService dispatcherExecutorService;

    private ScheduledExecutorService scheduledExecutorService;

    @Override
    public void shutdown() {}

    @Override
    public <T> Future<T> perform(final ResourceId resourceId,
                                 final Function<Resource, T> operation,
                                 final Consumer<Throwable> failure) {
        return getDispatcherExecutorService().submit(protectedCallable(resourceId, operation, failure));
    }

    @Override
    public <T> Future<T> perform(final Path path,
                                 final Function<Resource, T> operation,
                                 final Consumer<Throwable> failure) {
        return getDispatcherExecutorService().submit(protectedCallable(path, operation, failure));
    }

    @Override
    public <T> Future<T> performAfterDelay(final ResourceId resourceId,
                                           final long time, final TimeUnit timeUnit,
                                           final Function<Resource, T> operation,
                                           final Consumer<Throwable> failure) {
        final FutureTask<T> scheduled = new FutureTask<T>(protectedCallable(resourceId, operation, failure));
        getScheduledExecutorService().schedule(() -> getDispatcherExecutorService().submit(scheduled), time, timeUnit);
        return scheduled;
    }

    private <T> Callable<T> protectedCallable(final ResourceId resourceId,
                                              final Function<Resource, T> operation,
                                              final Consumer<Throwable> failure) {
        return () -> {
            try {
                final Resource resource = getResourceService().getAndAcquireResourceWithId(resourceId);
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

            final Resource resource;

            try {
                resource = getResourceService().getAndAcquireResourceAtPath(path);
            } catch (Exception ex) {
                failure.accept(ex);
                throw ex;
            }

            try {
                return performProtected(resource, operation);
            } catch (Exception ex) {
                failure.accept(ex);
                throw ex;
            } finally {
                getResourceService().release(resource);
            }

        };
    }

    private <T> T performProtected(final Resource resource,
                                   final Function<Resource, T> operation) {
        try (final ResourceLockService.Monitor m = getResourceLockService().getMonitor(resource.getId())){
            logger.trace("Applying operation for resource {}", resource.getId());
            return operation.apply(resource);
        } catch (Throwable th) {
            logger.error("Caught exception in protected operation {}", operation, th);
            throw th;
        } finally {
            logger.trace("Unlocked resource {}", resource.getId());
        }
    }

    public ExecutorService getDispatcherExecutorService() {
        return dispatcherExecutorService;
    }

    @Inject
    public void setDispatcherExecutorService(@Named(DISPATCHER_EXECUTOR_SERVICE) ExecutorService dispatcherExecutorService) {
        this.dispatcherExecutorService = dispatcherExecutorService;
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
