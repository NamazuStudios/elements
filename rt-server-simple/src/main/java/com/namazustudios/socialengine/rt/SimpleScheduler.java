package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.util.CompletionServiceLatch;
import com.namazustudios.socialengine.rt.util.LatchedExecutorServiceCompletionService;
import com.namazustudios.socialengine.rt.util.LatchedScheduledExecutorServiceCompletionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.namazustudios.socialengine.rt.remote.Worker.EXECUTOR_SERVICE;
import static com.namazustudios.socialengine.rt.remote.Worker.SCHEDULED_EXECUTOR_SERVICE;

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

    private ResourceLockService resourceLockService;

    private ResourceService resourceService;

    private ExecutorService dispatcherExecutorService;

    private ScheduledExecutorService scheduledExecutorService;

    private AtomicReference<Context> context = new AtomicReference<>();

    @Override
    public void start() {

        final var context = new Context();

        logger.info("Starting.");

        if (this.context.compareAndSet(null, context)) {
            logger.info("Started.");
        } else {
            throw new IllegalStateException("Scheduler already running.");
        }

    }

    @Override
    public void stop() {

        final var context = this.context.getAndSet(null);

        if (context == null) {
            throw new IllegalStateException("Scheduler not running.");
        } else {
            logger.info("Shutting down.");
            context.stop();
            logger.info("Finished shutting down.");
        }

    }

    private Context getContext() {
        final Context context = this.context.get();
        if (context == null) throw new IllegalStateException("Not running.");
        return context;
    }

    @Override
    public <T> Future<T> submit(Callable<T> tCallable) {
        return getContext().getDispatcher().submit(tCallable);
    }

    @Override
    public RunnableFuture<Void> scheduleUnlink(final Path path, final long delay, final TimeUnit timeUnit) {
        return shortCircuitFuture(
            () -> scheduleUnlink(path),
            r -> getContext().getScheduler().schedule(r , delay, timeUnit));
    }

    private Future<Void> scheduleUnlink(final Path path) {
        return getContext().getDispatcher().submit(() -> { getResourceService().unlinkPath(path, resource -> {

            final ResourceId resourceId = resource.getId();

            try {
                resource.close();
            } catch (ResourceNotFoundException ex) {
                logger.debug("No Resource found at path {}.  Disregarding.", ex);
            } catch (Exception ex) {
                logger.error("Caught exception unlinking resource {}", resourceId, ex);
            }

        });
        }, null);
    }

    @Override
    public RunnableFuture<Void> scheduleDestruction(final ResourceId resourceId, final long delay, final TimeUnit timeUnit) {
        return shortCircuitFuture(
            () -> scheduleDestruction(resourceId),
            r -> getContext().getScheduler().schedule(r , delay, timeUnit));
    }

    public Future<Void> scheduleDestruction(final ResourceId resourceId) {
        return getContext().getDispatcher().submit(() -> {
            try (final Monitor m = getResourceLockService().getMonitor(resourceId)) {
                getResourceService().destroy(resourceId);
            } catch (ResourceNotFoundException ex) {
                logger.debug("Resource already destroyed {}.  Disregarding.", resourceId, ex);
            } catch (Exception ex) {
                logger.error("Caught exception destroying Resource {}", resourceId, ex);
            }
        }, null);
    }

    @Override
    public <T> Future<T> perform(final ResourceId resourceId,
                                 final Function<Resource, T> operation,
                                 final Consumer<Throwable> failure) {
        return getContext().getDispatcher().submit(protectedCallable(resourceId, operation, failure));
    }

    @Override
    public <T> Future<T> perform(final Path path,
                                 final Function<Resource, T> operation,
                                 final Consumer<Throwable> failure) {
        return getContext().getDispatcher().submit(protectedCallable(path, operation, failure));
    }

    @Override
    public <T> Future<T> performAfterDelay(final ResourceId resourceId,
                                           final long time, final TimeUnit timeUnit,
                                           final Function<Resource, T> operation,
                                           final Consumer<Throwable> failure) {

        final var task = new FutureTask<T>(protectedCallable(resourceId, operation, failure));

        final var scheduled = getContext().getScheduler()
            .schedule(() -> getContext().getDispatcher().submit(task), time, timeUnit);

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

            final Resource resource;

            try (final Monitor m = getResourceLockService().getMonitor(resourceId)) {

                try {
                    resource = getResourceService().getAndAcquireResourceWithId(resourceId);
                } catch (Throwable th) {
                    failure.accept(th);
                    throw th;
                }

                try {
                    return performProtected(resource, operation);
                } catch (Throwable th) {
                    failure.accept(th);
                    throw th;
                } finally {
                    getResourceService().release(resource);
                }

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
        try (final Monitor m = getResourceLockService().getMonitor(resource.getId())){
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
    public void setDispatcherExecutorService(@Named(EXECUTOR_SERVICE) ExecutorService dispatcherExecutorService) {
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


    private static FutureTask<Void> shortCircuitFuture(final Runnable runnable,
                                                       final Function<Runnable, Future<?>> delegateFutureSupplier) {
        return new FutureTask<>(runnable, null) {

            final Future<?> delegate = delegateFutureSupplier.apply(this);

            @Override
            public void run() {
                cancel(false);
                super.run();
            }

            @Override
            public void done() {
                if (isCancelled()) delegate.cancel(false);
            }

        };
    }

    private class Context {

        private final CompletionServiceLatch latch = new CompletionServiceLatch();

        private final LatchedExecutorServiceCompletionService dispatcher =
            new LatchedExecutorServiceCompletionService(getDispatcherExecutorService(), latch);

        private final LatchedScheduledExecutorServiceCompletionService scheduler =
            new LatchedScheduledExecutorServiceCompletionService(getScheduledExecutorService(), latch);

        public LatchedExecutorServiceCompletionService getDispatcher() {
            return dispatcher;
        }

        public LatchedScheduledExecutorServiceCompletionService getScheduler() {
            return scheduler;
        }

        private void stop() {
            latch.stop();
        }

    }

}
