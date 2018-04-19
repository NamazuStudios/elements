package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.NoSuchTaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class SimpleSchedulerContext implements SchedulerContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleSchedulerContext.class);

    private Scheduler scheduler;

    @Override
    public void resumeTaskAfterDelay(ResourceId resourceId, long time, TimeUnit timeUnit, TaskId taskId) {
        getScheduler().resumeTaskAfterDelay(resourceId, time, timeUnit, taskId,
                                            th -> logger.error("Caught exception resuming.", th));
    }

    @Override
    public void resumeFromNetwork(ResourceId resourceId, TaskId taskId, Object result) {
        getScheduler().performV(resourceId, resource -> resumeFromNetwork(resource, taskId, result),
                                            th -> logger.error("Caught exception resuming.", th));
    }

    private void resumeFromNetwork(final Resource resource, final TaskId taskId, final Object result) {
        try {
            resource.resumeFromNetwork(taskId, result);
        } catch (NoSuchTaskException ex) {
            logger.info("Ignoring dead task: {}", ex.getTaskId());
        }
    }

    @Override
    public void resumeWithError(final ResourceId resourceId, final TaskId taskId, final Throwable throwable) {
        getScheduler().performV(resourceId, resource -> resumeWithError(resource, taskId, throwable),
                                            th -> logger.error("Caught exception resuming.", th));
    }

    private void resumeWithError(final Resource resource, final TaskId taskId, final Throwable throwable) {
        try {
            resource.resumeWithError(taskId, throwable);
        } catch (NoSuchTaskException ex) {
            logger.info("Ignoring dead task: {}", ex.getTaskId());
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
