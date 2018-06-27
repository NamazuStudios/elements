package com.namazustudios.socialengine.rt;

import com.google.common.collect.Streams;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MINUTES;

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
    public Future<Void> scheduleUnlink(final Path path) {
        return getDispatcherExecutorService().submit(() -> {
            getResourceService().unlinkPath(path, resource -> {

                final ResourceId resourceId = resource.getId();

                try {
                    resource.close();
                } catch (ResourceNotFoundException ex) {
                    logger.debug("No Resource found at path {}.  Disregarding.", ex);
                } catch (Exception ex) {
                    logger.error("Caught exception destroying Resource {}", resourceId, ex);
                }

            });
        }, null);
    }

    @Override
    public Future<Void> scheduleDestruction(final ResourceId resourceId) {
        return getDispatcherExecutorService().submit(() -> {
            try (final ResourceLockService.Monitor m = getResourceLockService().getMonitor(resourceId)) {
                getResourceService().destroy(resourceId);
            } catch (Exception ex) {
                logger.error("Caught exception destroying Resource {}", resourceId, ex);
            }
        }, null);
    }

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

        final FutureTask<T> task = new FutureTask<T>(protectedCallable(resourceId, operation, failure));

        final Future<?> scheduled = getScheduledExecutorService()
            .schedule(() -> getDispatcherExecutorService().submit(task), time, timeUnit);

        return new Future<T>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return task.cancel(mayInterruptIfRunning) && scheduled.cancel(mayInterruptIfRunning);
            }

            @Override
            public boolean isCancelled() {
                return task.isCancelled();
            }

            @Override
            public boolean isDone() {
                return task.isDone();
            }

            @Override
            public T get() throws InterruptedException, ExecutionException {
                return task.get();
            }

            @Override
            public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return task.get(timeout, unit);
            }
        };

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

    @Override
    public void shutdown() {
        try {

            logger.info("Shutting down dispatcher threads.");
            dispatcherExecutorService.shutdown();

            logger.info("Shutting down scheduler threads.");
            scheduledExecutorService.shutdownNow();

            if (scheduledExecutorService.awaitTermination(5, MINUTES)) {
                logger.info("Shut down scheduler threads.");
            } else {
                logger.error("Timed out shutting down scheduler threads.");
            }

            if (dispatcherExecutorService.awaitTermination(5, TimeUnit.MINUTES)) {
                logger.info("Shut down dispatcher threads.");
            } else {
                logger.error("Timed out shutting down dispatcher threads.");
            }

        } catch (InterruptedException ex) {
            throw new InternalException(ex);
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
