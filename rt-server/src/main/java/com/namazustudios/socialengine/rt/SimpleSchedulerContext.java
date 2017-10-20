package com.namazustudios.socialengine.rt;

import javax.inject.Inject;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class SimpleSchedulerContext implements SchedulerContext {

    private Scheduler scheduler;

    @Override
    public Future<Void> resumeTaskAfterDelay(ResourceId resourceId, long time, TimeUnit timeUnit, TaskId taskId) {
        return getScheduler().resumeTaskAfterDelay(resourceId, time, timeUnit, taskId);
    }

    @Override
    public <T> Future<T> perform(Path path, Function<Resource, T> operation) {
        return getScheduler().perform(path, operation);
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

}
