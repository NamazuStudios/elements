package dev.getelements.elements.rt.remote.provider;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import java.util.concurrent.ScheduledExecutorService;

import static dev.getelements.elements.rt.Constants.SCHEDULER_THREADS;
import static dev.getelements.elements.rt.remote.Instance.THREAD_GROUP;
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
