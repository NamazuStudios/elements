package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.NoSuchTaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SuppressWarnings("Duplicates")
public class SimpleHandlerContext implements HandlerContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleHandlerContext.class);

    private long timeout;

    private Scheduler scheduler;

    private ResourceService resourceService;

    private RetainedHandlerService retainedHandlerService;

    private SingleUseHandlerService singleUseHandlerService;

    @Override
    public void start() {
        getSingleUseHandlerService().start();
    }

    @Override
    public void stop() {
        getSingleUseHandlerService().stop();
    }

    @Override
    public void invokeSingleUseHandlerAsync(
            final Consumer<Object> success, final Consumer<Throwable> failure,
            final Attributes attributes, final String module,
            final String method, final Object... args) {

        final TaskId taskId;
        final AtomicReference<Future<Void>> futureAtomicReference = new AtomicReference<>();

        final Consumer<Throwable> _failure = t -> {
            try {
                final Future<Void> f = futureAtomicReference.get();
                if (f != null && !f.isDone()) futureAtomicReference.get().cancel(false);
            } catch (Exception ex) {
                logger.error("Caught exception in handler.", ex);
            }
        };

        final Consumer<Object> _success = o -> {
            try {
                final Future<Void> f = futureAtomicReference.get();
                if (f != null && !f.isDone()) futureAtomicReference.get().cancel(false);
            } catch (Throwable th) {
                _failure.accept(th);
            }
        };

        try {
            taskId = getSingleUseHandlerService().perform(_success.andThen(success), _failure.andThen(failure),
                                                          module, attributes, method, args);
        } catch (Exception ex) {
            failure.accept(ex);
            throw new InternalException(ex);
        }

        futureAtomicReference.set(scheduleTimeout(taskId, failure));

    }

    @Override
    public void invokeRetainedHandlerAsync(
            final Consumer<Object> success, final Consumer<Throwable> failure,
            final Attributes attributes, final String module,
            final String method, final Object... args) {

        final TaskId taskId;
        final AtomicReference<Future<Void>> futureAtomicReference = new AtomicReference<>();

        final Consumer<Throwable> _failure = t -> {
            try {
                final Future<Void> f = futureAtomicReference.get();
                if (f != null && !f.isDone()) futureAtomicReference.get().cancel(false);
            } catch (Exception ex) {
                logger.error("Caught exception in handler.", ex);
            }
        };

        final Consumer<Object> _success = o -> {
            try {
                final Future<Void> f = futureAtomicReference.get();
                if (f != null && !f.isDone()) futureAtomicReference.get().cancel(false);
            } catch (Throwable th) {
                _failure.accept(th);
            }
        };

        try {
            taskId = getRetainedHandlerService().perform(_success.andThen(success), _failure.andThen(failure),
                                                         module, attributes, method, args);
        } catch (Exception ex) {
            failure.accept(ex);
            throw new InternalException(ex);
        }

        futureAtomicReference.set(scheduleTimeout(taskId, failure));

    }

    private Future<Void> scheduleTimeout(final TaskId taskId, final Consumer<Throwable> failure) {
        try {
            return getScheduler().performAfterDelayV(taskId.getResourceId(), getTimeout(), MILLISECONDS, r -> {
                try {
                    r.resumeWithError(taskId, new TimeoutException("Handler timed out."));
                    logger.debug("Timing out task {}", taskId);
                } catch (NoSuchTaskException ex) {
                    logger.trace("Ignoring dead task.", ex);
                }
            }, failure);
        } catch (Exception ex) {
            failure.accept(ex);
            logger.error("Error timing out task {}", taskId);
            throw new InternalException(ex);
        }
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public RetainedHandlerService getRetainedHandlerService() {
        return retainedHandlerService;
    }

    @Inject
    public void setRetainedHandlerService(RetainedHandlerService retainedHandlerService) {
        this.retainedHandlerService = retainedHandlerService;
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
