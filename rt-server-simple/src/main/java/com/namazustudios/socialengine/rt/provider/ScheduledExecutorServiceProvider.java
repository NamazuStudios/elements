package com.namazustudios.socialengine.rt.provider;

import com.namazustudios.socialengine.rt.SimpleScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.namazustudios.socialengine.rt.Constants.SCHEDULER_THREADS;
import static java.lang.String.format;
import static java.util.concurrent.Executors.newScheduledThreadPool;

public class ScheduledExecutorServiceProvider implements Provider<ScheduledExecutorService> {

    private Provider<Integer> schedulerPoolSizeProvider;

    @Override
    public ScheduledExecutorService get() {

        final AtomicInteger threadCount = new AtomicInteger();
        final Logger logger = LoggerFactory.getLogger(SimpleScheduler.class);
        final String name = format("%s.%s", SimpleScheduler.class.getSimpleName(), "timer");

        return newScheduledThreadPool(getSchedulerPoolSizeProvider().get(),
                                      r -> newThread(r, name, threadCount, logger));

    }

    private Thread newThread(final Runnable runnable, final String name,
                             final AtomicInteger threadCount, final Logger logger) {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName(format("%s #%d", name, threadCount.incrementAndGet()));
        thread.setUncaughtExceptionHandler((t , e) -> logger.error("Fatal Error: {}", t, e));
        return thread;
    }

    public Provider<Integer> getSchedulerPoolSizeProvider() {
        return schedulerPoolSizeProvider;
    }

    @Inject
    public void setSchedulerPoolSizeProvider(@Named(SCHEDULER_THREADS) Provider<Integer> schedulerPoolSizeProvider) {
        this.schedulerPoolSizeProvider = schedulerPoolSizeProvider;
    }

}
