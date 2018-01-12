package com.namazustudios.socialengine.rt;

import java.util.concurrent.TimeUnit;

public class ClusterClientSchedulerContext implements SchedulerContext {

    @Override
    public void resumeTaskAfterDelay(final ResourceId resourceId, final long time, final TimeUnit timeUnit, final TaskId taskId) {

    }

    @Override
    public void resumeFromNetwork(final ResourceId resourceId, final TaskId taskId, final Object result) {

    }

    @Override
    public void resumeWithError(final ResourceId resourceId, final TaskId taskId, final Throwable throwable) {

    }

}
