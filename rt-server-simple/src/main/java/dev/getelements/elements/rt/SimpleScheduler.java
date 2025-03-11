package dev.getelements.elements.rt;

import dev.getelements.elements.rt.ResourceService.ResourceTransaction;
import dev.getelements.elements.rt.exception.ResourceNotFoundException;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.remote.provider.ExecutorServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.getelements.elements.rt.remote.Instance.EXECUTOR_SERVICE;
import static dev.getelements.elements.rt.remote.Instance.SCHEDULED_EXECUTOR_SERVICE;
import static java.lang.String.format;
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

    private ResourceService resourceService;

    private ExecutorServiceFactory<ExecutorService> executorServiceFactory;

    private ExecutorServiceFactory<ScheduledExecutorService> scheduledExecutorServiceFactory;

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
        return getContext().getDispatcher().submit(() -> getResourceService().unlinkPath(path, resource -> {

            final ResourceId resourceId = resource.getId();

            try {
                resource.close();
            } catch (ResourceNotFoundException ex) {
                logger.debug("No Resource found at path {}.  Disregarding.", path, ex);
            } catch (Exception ex) {
                logger.error("Caught exception unlinking resource {}", resourceId, ex);
            }

        }), null);
    }

    @Override
    public RunnableFuture<Void> scheduleDestruction(final ResourceId resourceId, final long delay, final TimeUnit timeUnit) {
        return shortCircuitFuture(
            () -> scheduleDestruction(resourceId),
            r -> getContext().getScheduler().schedule(r , delay, timeUnit));
    }

    public Future<Void> scheduleDestruction(final ResourceId resourceId) {
        return getContext().getDispatcher().submit(() -> {
            try {
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
                                 final Function<ResourceTransaction, T> operation,
                                 final Consumer<Throwable> failure) {
        return getContext().getDispatcher().submit(protectedCallable(resourceId, operation, failure));
    }

    @Override
    public <T> Future<T> perform(final Path path,
                                 final Function<ResourceTransaction, T> operation,
                                 final Consumer<Throwable> failure) {
        return getContext().getDispatcher().submit(protectedCallable(path, operation, failure));
    }

    @Override
    public <T> Future<T> performAfterDelay(final ResourceId resourceId,
                                           final long time, final TimeUnit timeUnit,
                                           final Function<ResourceTransaction, T> operation,
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
                                              final Function<ResourceTransaction, T> operation,
                                              final Consumer<Throwable> failure) {
        return () -> {
            try (var txn = getResourceService().acquireWithTransaction(resourceId)) {
                try {
                    var result = operation.apply(txn);
                    txn.commit();
                    return result;
                } catch (Exception ex) {
                    failure.accept(ex);
                    txn.rollback();
                    throw ex;
                }
            }
        };
    }

    private <T> Callable<T> protectedCallable(final Path path,
                                              final Function<ResourceTransaction, T> operation,
                                              final Consumer<Throwable> failure) {
        return () -> {
            try (var txn = getResourceService().acquireWithTransaction(path)) {
                try {
                    var result = operation.apply(txn);
                    txn.commit();
                    return result;
                } catch (Exception ex) {
                    failure.accept(ex);
                    txn.rollback();
                    throw ex;
                }
            }
        };
    }

    public ExecutorServiceFactory<ExecutorService> getExecutorServiceFactory() {
        return executorServiceFactory;
    }

    @Inject
    public void setExecutorServiceFactory(@Named(EXECUTOR_SERVICE) ExecutorServiceFactory<ExecutorService> executorServiceFactory) {
        this.executorServiceFactory = executorServiceFactory;
    }

    public ExecutorServiceFactory<ScheduledExecutorService> getScheduledExecutorServiceFactory() {
        return scheduledExecutorServiceFactory;
    }

    @Inject
    public void setScheduledExecutorServiceFactory(@Named(SCHEDULED_EXECUTOR_SERVICE) ExecutorServiceFactory<ScheduledExecutorService> scheduledExecutorServiceFactory) {
        this.scheduledExecutorServiceFactory = scheduledExecutorServiceFactory;
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
            public void done() {
                if (isCancelled()) delegate.cancel(false);
            }

        };
    }

    private class Context {

        private final ExecutorService dispatcher = getExecutorServiceFactory()
            .getService(format("%s.scheduler", SimpleScheduler.class.getName()));

        private final ScheduledExecutorService scheduler = getScheduledExecutorServiceFactory()
            .getService(format("%s.dispatcher", SimpleScheduler.class.getName()));

        public ExecutorService getDispatcher() {
            return dispatcher;
        }

        public ScheduledExecutorService getScheduler() {
            return scheduler;
        }

        private void stop() {

            dispatcher.shutdown();
            scheduler.shutdownNow();

            try {

                if (!scheduler.awaitTermination(1, MINUTES)) {
                    logger.error("Timed out shutting down scheduler.");
                }

                if (!dispatcher.awaitTermination(1, MINUTES)) {
                    logger.error("Timed out shutting down dispatcher.");
                }

            } catch (InterruptedException ex) {
                logger.error("Interrupted while shutting down.");
            }

        }

    }

}
