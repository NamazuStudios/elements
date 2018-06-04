package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.HandlerTimeoutException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.Constants.HANDLER_TIMEOUT_MSEC;
import static com.namazustudios.socialengine.rt.Path.fromComponents;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.joining;

public class SimpleHandlerContext implements HandlerContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleHandlerContext.class);

    private long timeout;

    private IndexContext indexContext;

    private ResourceContext resourceContext;

    private SingleUseHandlerService singleUseHandlerService;

    private static final ScheduledExecutorService reapers = Executors.newSingleThreadScheduledExecutor(r -> {
        final Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName(SimpleHandlerContext.class.getSimpleName() + ".reaper");
        thread.setUncaughtExceptionHandler(((t, e) -> logger.error("Fatal Error: {}", t, e)));
        return thread;
    });

    @Override
    public void invokeSingleUseHandlerAsync(
            final Consumer<Object> success, final Consumer<Throwable> failure,
            final Attributes attributes, final String module,
            final String method, final Object... args) {
        try {
            getSingleUseHandlerService().perform(attributes, module, r -> r
                .getMethodDispatcher(method)
                .params(args)
                .dispatch(success, failure));
        } catch (Exception ex) {
            failure.accept(ex);
            throw new InternalException(ex);
        }
    }

    @Override
    public void invokeRetainedHandlerAsync(
            final Consumer<Object> success, final Consumer<Throwable> failure,
            final Attributes attributes, final String module,
            final String method, final Object... args) {

        final AtomicBoolean finished = new AtomicBoolean();
        final Path path = fromComponents("tmp", "handler", randomUUID().toString());
        final ResourceId resourceId = getResourceContext().createAttributes(module, path, attributes);
        final ScheduledFuture<?> timeoutScheduledFuture = scheduleTimeout(finished, resourceId, failure);

        try {
            getResourceContext().invokeAsync(
                succeedAndUnlink(finished, timeoutScheduledFuture, path, success, failure),
                failure(finished, timeoutScheduledFuture, resourceId, failure, module, method, args),
                resourceId, method, args);
        } catch (RuntimeException ex) {
            unlinkAndLog(path);
            failure.accept(ex);
            throw ex;
        } catch (Exception ex) {
            destroyAndLog(resourceId);
            failure.accept(ex);
            throw new InternalException(ex);
        }

    }

    private ScheduledFuture<?> scheduleTimeout(final AtomicBoolean finished,
                                               final ResourceId resourceId,
                                               final Consumer<Throwable> errorConsumer) {
        return reapers.schedule(() -> {
            if (finished.compareAndSet(false, true)) {
                final HandlerTimeoutException ex = new HandlerTimeoutException();
                errorConsumer.accept(ex);
                logger.error("Resource {} timed out.", resourceId, ex);
            }
        }, getTimeout(), MILLISECONDS);
    }

    private Consumer<Object> succeedAndUnlink(
            final AtomicBoolean finished,
            final ScheduledFuture<?> timeoutScheduledFuture,
            final Path path,
            final Consumer<Object> success,
            final Consumer<Throwable> failure) {
        return o -> {
            if (finished.compareAndSet(false, true)) {
                try {
                    success.accept(o);
                } catch (Exception ex) {
                    logger.error("Exception in handler context.", ex);
                    failure.accept(ex);
                } finally {
                    timeoutScheduledFuture.cancel(false);
                    unlinkAndLog(path);
                }
            }
        };
    }

    private <T extends Throwable> Consumer<T> failure(
            final AtomicBoolean finished,
            final ScheduledFuture<?> timeoutScheduledFuture,
            final ResourceId resourceId,
            final Consumer<Throwable> consumer,
            final String module,
            final String method,
            final Object[] args) {
        return th -> {
            if (finished.compareAndSet(false, true)) {
                try {
                    logger.error("Unsuccessful Result for {}.{}({})", module, method,
                        Stream.of(args)
                        .map(a -> a == null ? null : a.toString())
                        .collect(joining(",")), th);
                    consumer.accept(th);
                } catch (Exception ex) {
                    logger.error("Exception in handler context.", ex);
                } finally {
                    timeoutScheduledFuture.cancel(false);
                    destroyAndLog(resourceId);
                }
            }
        };
    }

    private void unlinkAndLog(final Path path) {
        getIndexContext().unlinkAsync(path,
            v -> logger.debug("Destroyed {}.", path),
            th -> logger.error("Failed to destroy {}", path));
    }

    private void destroyAndLog(final ResourceId resourceId) {
        getResourceContext().destroyAsync(
            v -> logger.debug("Destroyed {}.", resourceId),
            th -> logger.error("Failed to destroy {}", resourceId),
            resourceId);
    }

    public IndexContext getIndexContext() {
        return indexContext;
    }

    @Inject
    public void setIndexContext(IndexContext indexContext) {
        this.indexContext = indexContext;
    }

    public ResourceContext getResourceContext() {
        return resourceContext;
    }

    @Inject
    public void setResourceContext(ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
    }

    public SingleUseHandlerService getSingleUseHandlerService() {
        return singleUseHandlerService;
    }

    @Inject
    public void setSingleUseHandlerService(SingleUseHandlerService singleUseHandlerService) {
        this.singleUseHandlerService = singleUseHandlerService;
    }

    public long getTimeout() {
        return timeout;
    }

    @Inject
    public void setTimeout(@Named(HANDLER_TIMEOUT_MSEC) long timeout) {
        this.timeout = timeout;
    }

}
