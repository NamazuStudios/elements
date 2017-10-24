package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class SimpleSchedulerContext implements SchedulerContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleSchedulerContext.class);

    private Scheduler scheduler;

    @Override
    public Future<Void> resumeTaskAfterDelay(ResourceId resourceId, long time, TimeUnit timeUnit, TaskId taskId) {
        return getScheduler().resumeTaskAfterDelay(resourceId, time, timeUnit, taskId, th -> logger.error("Caught exception resuming.", th));
    }

    @Override
    public Future<Void> resumeFromNetwork(ResourceId resourceId, TaskId taskId, Object result) {
        return getScheduler().performV(resourceId, resource -> resource.resumeFromNetwork(taskId, result),
                                                   th -> logger.error("Caught exception resuming.", th));
    }

    @Override
    public Future<Void> resumeWithError(ResourceId resourceId, TaskId taskId, Throwable throwable) {
        return getScheduler().performV(resourceId, resource -> resource.resumeWithError(taskId, throwable),
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
