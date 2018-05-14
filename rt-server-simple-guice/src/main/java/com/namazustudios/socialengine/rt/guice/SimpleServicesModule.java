package com.namazustudios.socialengine.rt.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.provider.CachedThreadPoolProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Constants.SCHEDULER_THREADS;
import static com.namazustudios.socialengine.rt.SimpleScheduler.DISPATCHER_EXECUTOR_SERVICE;
import static com.namazustudios.socialengine.rt.SimpleScheduler.SCHEDULED_EXECUTOR_SERVICE;
import static java.lang.String.format;
import static java.util.concurrent.Executors.newScheduledThreadPool;

/**
 * Creates the simple internal
 * <p>
 * <p>
 * Created by patricktwohig on 9/22/15.
 */
public class SimpleServicesModule extends PrivateModule {


    private Runnable bindSchedulerThreads = () -> {};

    public SimpleServicesModule withSchedulerThreads(int threads) {
        bindSchedulerThreads = () -> bind(Integer.class)
                .annotatedWith(named(SCHEDULER_THREADS))
                .toInstance(threads);
        return this;
    }

    @Override
    protected void configure() {

        bindSchedulerThreads.run();

        final Provider<Integer> schedulerPoolSizeProvider = getProvider(Key.get(Integer.class, named(SCHEDULER_THREADS)));

        // The actual underlying services
        bind(Scheduler.class).to(SimpleScheduler.class).asEagerSingleton();
        bind(ResourceLockService.class).to(SimpleResourceLockService.class).asEagerSingleton();
        bind(ResourceService.class).to(SimpleResourceService.class).asEagerSingleton();

        bind(new TypeLiteral<OptimisticLockService<Deque<Path>>>() {})
            .toProvider(() -> new ProxyLockService<>(Deque.class));

        bind(new TypeLiteral<OptimisticLockService<ResourceId>>() {})
            .to(SimpleResourceIdOptimisticLockService.class);

        bind(ScheduledExecutorService.class)
            .annotatedWith(named(SCHEDULED_EXECUTOR_SERVICE))
            .toProvider(() -> scheduledExecutorService(schedulerPoolSizeProvider));

        bind(ExecutorService.class)
            .annotatedWith(named(DISPATCHER_EXECUTOR_SERVICE))
            .toProvider(new CachedThreadPoolProvider(SimpleScheduler.class, "dispatch"));

        expose(Scheduler.class);
        expose(ResourceService.class);

    }

    private ScheduledExecutorService scheduledExecutorService(final Provider<Integer> schedulerPoolSizeProvider) {
        final AtomicInteger threadCount = new AtomicInteger();
        final Logger logger = LoggerFactory.getLogger(SimpleScheduler.class);
        final String name = format("%s.%s", SimpleScheduler.class.getSimpleName(), "timer");
        return newScheduledThreadPool(schedulerPoolSizeProvider.get(), r -> newThread(r, name, threadCount, logger));
    }

    private Thread newThread(final Runnable runnable, final String name,
                             final AtomicInteger threadCount, final Logger logger) {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName(format("%s #%d", name, threadCount.incrementAndGet()));
        thread.setUncaughtExceptionHandler((t , e) -> logger.error("Fatal Error: {}", t, e));
        return thread;
    }

}
