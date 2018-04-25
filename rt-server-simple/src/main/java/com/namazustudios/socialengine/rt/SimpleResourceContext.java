package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;


public class SimpleResourceContext implements ResourceContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleResourceContext.class);

    public static final String EXECUTOR_SERVICE = "com.namazustudios.socialengine.rt.SimpleResourceContext.executorService";

    private Scheduler scheduler;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private ExecutorService executorService;

    @Override
    public ResourceId createAttributes(final String module, final Path path, final Attributes attributes, final Object... args) {
        logger.info("Loading module {} -> {}", module, path);
        final Resource resource = getResourceLoader().load(module, attributes, args);
        getResourceService().addResource(path, resource);
        return resource.getId();
    }

    @Override
    public Future<ResourceId> createAttributesAsync(final Consumer<ResourceId> success, final Consumer<Throwable> failure,
                                                    final String module, final Path path, final Attributes attributes, final Object... args) {
        return getExecutorService().submit(() -> {
            try {
                final ResourceId resourceId = createAttributes(module, path, attributes, args);
                success.accept(resourceId);
                return resourceId;
            } catch (Throwable th) {
                logger.error("Caught Exception loading module {} -> {}", module, path, th);
                failure.accept(th);
                throw th;
            }
        });
    }

    public Future<Void> destroyAsync(final Consumer<Void> success,
                                     final Consumer<Throwable> failure,
                                     final ResourceId resourceId) {
        // The Resource must be locked in order to properly destroy it because it invovles mutating the Resource itself.
        // if we try to destroy it without using the scheduler, we could end up with two threads accessing it at the
        // same time, which is no good.
        return getScheduler().performV(resourceId, r -> {
            try {
                getResourceService().destroy(resourceId);
                success.accept(null);
            } catch (Throwable throwable) {
                failure.accept(throwable);
            }
        }, failure.andThen(th -> logger.error("Failure", th)));
    }

    @Override
    public Future<Object> invokeAsync(final Consumer<Object> success, final Consumer<Throwable> failure,
                                      final ResourceId resourceId, final String method, final Object... args) {

        final InvocationFuture invocationFuture = new InvocationFuture();

        getScheduler().perform(resourceId, resource -> doInvoke(invocationFuture.success().andThen(success),
                                                                invocationFuture.failure().andThen(failure),
                                                                resource, method, args), failure);

        return invocationFuture;

    }

    @Override
    public Future<Object> invokePathAsync(final Consumer<Object> success, final Consumer<Throwable> failure,
                                          final Path path, final String method, final Object... args) {

        final InvocationFuture invocationFuture = new InvocationFuture();

        getScheduler().perform(path, resource -> doInvoke(invocationFuture.success().andThen(success),
                                                          invocationFuture.failure().andThen(failure),
                                                          resource, method, args), failure);

        return invocationFuture;

    }

    private TaskId doInvoke(final Consumer<Object> success, final Consumer<Throwable> failure,
                                    final Resource resource, final String method, final Object... args) {
        try {
            return resource.getMethodDispatcher(method).params(args).dispatch(success, failure);
        } catch (Throwable th) {
            failure.accept(th);
            throw th;
        }
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

    private static class InvocationFuture implements Future<Object> {

        private final CountDownLatch countDownLatch = new CountDownLatch(1);

        private final AtomicReference<State> state = new AtomicReference<>(State.PENDING);

        private final AtomicReference<Object> result = new AtomicReference<>();

        private final AtomicReference<Throwable> throwable = new AtomicReference<>();

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (state.compareAndSet(State.PENDING, State.CANCELED)) {
                countDownLatch.countDown();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean isCancelled() {
            return state.get() == State.CANCELED;
        }

        @Override
        public boolean isDone() {
            switch (state.get()) {
                case DONE:
                case CANCELED:
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException {
            countDownLatch.await();
            return getResultOrThrow();
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            countDownLatch.await(timeout, unit);
            return getResultOrThrow();
        }

        private Object getResultOrThrow() throws ExecutionException {

            final Throwable throwable = this.throwable.get();

            if (this.throwable.get() != null) {
                throw new ExecutionException(throwable);
            }

            return result.get();

        }

        private enum State { PENDING, DONE, CANCELED }

        public Consumer<Object> success() {
            return object -> {
                if (throwable.get() == null && result.compareAndSet(null, object)) {
                    countDownLatch.countDown();
                }
            };
        }

        public Consumer<Throwable> failure() {
            return th -> {
                if (throwable.compareAndSet(null, th)) {
                    countDownLatch.countDown();
                }
            };
        }

    }

    @Override
    public void destroyAllResources() {
        getResourceService().removeAndCloseAllResources();
    }

    @Override
    public Future<Void> destroyAllResourcesAsync(Consumer<Void> success, Consumer<Throwable> failure) {
        return getExecutorService().submit(() -> {

            try {
                getResourceService().removeAndCloseAllResources();
                success.accept(null);
            } catch (Throwable th) {
                failure.accept(th);
                throw th;
            }

            return null;
        });
    }

}
