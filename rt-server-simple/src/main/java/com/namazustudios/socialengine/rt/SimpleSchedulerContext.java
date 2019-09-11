package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.NoSuchTaskException;
import com.namazustudios.socialengine.rt.id.TaskId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class SimpleSchedulerContext implements SchedulerContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleSchedulerContext.class);

    private Scheduler scheduler;

    @Override
    public void stop() {
        getScheduler().shutdown();
    }

    @Override
    public void resume(final TaskId taskId, final Object ... results) {
        getScheduler().performV(taskId.getResourceId(),
            r  -> { logger.trace("Resumed task {}:{}", taskId); r.resume(taskId, results); },
            th ->   logger.error("Caught exception resuming {}.", taskId, th)
        );
    }

    @Override
    public void resumeTaskAfterDelay(final TaskId taskId, final long time, final TimeUnit timeUnit) {
        getScheduler().resumeTaskAfterDelay(
            taskId, time, timeUnit,
            () -> logger.trace("Resumed task {}:{}", taskId),
            th -> logger.error("Caught exception resuming {}.", taskId, th)
        );
    }

    public void resumeTaskAfterDelay(long time, final TimeUnit timeUnit,
                                     final TaskId taskId,
                                     final Runnable resumed) {
        getScheduler().resumeTaskAfterDelay(
            taskId, time, timeUnit,
            () -> { logger.trace("Resumed task {}", taskId); resumed.run(); },
            th -> { logger.error("Caught exception resuming {}.", taskId, th); resumed.run(); } );
    }

    @Override
    public void resumeFromNetwork(final TaskId taskId, final Object result) {
        getScheduler().performV(taskId.getResourceId(),
            resource -> resumeFromNetwork(resource, taskId, result),
            th -> logger.error("Caught exception resuming {}.", taskId, th));
    }

    private void resumeFromNetwork(final Resource resource, final TaskId taskId, final Object result) {
        try {
            resource.resumeFromNetwork(taskId, result);
        } catch (NoSuchTaskException ex) {
            logger.debug("Ignoring dead task: {}", ex.getTaskId());
        }
    }

    @Override
    public void resumeWithError(final TaskId taskId, final Throwable throwable) {
        getScheduler().performV(taskId.getResourceId(),
            resource -> resumeWithError(resource, taskId, throwable),
            th -> logger.error("Caught exception resuming.", th));
    }

    private void resumeWithError(final Resource resource, final TaskId taskId, final Throwable throwable) {
        try {
            resource.resumeWithError(taskId, throwable);
        } catch (NoSuchTaskException ex) {
            logger.debug("Ignoring dead task: {}", ex.getTaskId());
        }
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

}
