package com.namazustudios.socialengine.rt.remote.provider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.concurrent.ScheduledExecutorService;

import static com.namazustudios.socialengine.rt.Constants.SCHEDULER_THREADS;
import static com.namazustudios.socialengine.rt.remote.Instance.THREAD_GROUP;
import static java.util.concurrent.Executors.newScheduledThreadPool;

public class ScheduledExecutorServiceProvider implements Provider<ExecutorServiceFactory<ScheduledExecutorService>> {

    private Provider<ThreadGroup>threadGroupProvider;

    private Provider<Integer> schedulerPoolSizeProvider;

    @Override
    public ExecutorServiceFactory<ScheduledExecutorService> get() {
        return name -> {
            final var size = getSchedulerPoolSizeProvider().get();
            final var group = getThreadGroupProvider().get();
            final var factory = new InstanceThreadFactory(group, name);
            return newScheduledThreadPool(size, factory);
        };
    }

    public Provider<ThreadGroup> getThreadGroupProvider() {
        return threadGroupProvider;
    }

    @Inject
    public void setThreadGroupProvider(@Named(THREAD_GROUP) Provider<ThreadGroup> threadGroupProvider) {
        this.threadGroupProvider = threadGroupProvider;
    }

    public Provider<Integer> getSchedulerPoolSizeProvider() {
        return schedulerPoolSizeProvider;
    }

    @Inject
    public void setSchedulerPoolSizeProvider(@Named(SCHEDULER_THREADS) Provider<Integer> schedulerPoolSizeProvider) {
        this.schedulerPoolSizeProvider = schedulerPoolSizeProvider;
    }

}
