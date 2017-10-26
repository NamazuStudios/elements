package com.namazustudios.socialengine.rt;

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
        getScheduler().performV(resourceId, resource -> resource.resumeFromNetwork(taskId, result),
                                            th -> logger.error("Caught exception resuming.", th));
    }

    @Override
    public void resumeWithError(ResourceId resourceId, TaskId taskId, Throwable throwable) {
        getScheduler().performV(resourceId, resource -> resource.resumeWithError(taskId, throwable),
                                            th -> logger.error("Caught exception resuming.", th));
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

}
