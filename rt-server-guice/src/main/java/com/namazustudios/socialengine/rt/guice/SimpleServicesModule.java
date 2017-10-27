package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.SimpleScheduler.SCHEDULED_EXECUTOR_SERVICE;
import static java.lang.String.format;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newScheduledThreadPool;

/**
 * Creates the simple internal
 *
 *
 * Created by patricktwohig on 9/22/15.
 */
public class SimpleServicesModule extends PrivateModule {

    private static final int CORE_POOL_SIZE = 5;

    @Override
    protected void configure() {

        // The main context for the application
        bind(Context.class).to(SimpleContext.class).asEagerSingleton();

        // The sub-contexts associated with the main context
        bind(IndexContext.class).to(SimpleIndexContext.class);
        bind(ResourceContext.class).to(SimpleResourceContext.class);
        bind(SchedulerContext.class).to(SimpleSchedulerContext.class);

        // The actual underlying services
        bind(Scheduler.class).to(SimpleScheduler.class).asEagerSingleton();
        bind(ResourceLockService.class).to(SimpleResourceLockService.class).asEagerSingleton();
        bind(ResourceService.class).to(SimpleResourceService.class).asEagerSingleton();

        bind(new TypeLiteral<OptimisticLockService<Deque<Path>>>(){})
                .toProvider(() -> new ProxyLockService<>(Deque.class));

        bind(new TypeLiteral<OptimisticLockService<ResourceId>>(){})
                .to(SimpleResourceIdOptimisticLockService.class);

        bind(ExecutorService.class)
                .annotatedWith(named(SimpleResourceContext.EXECUTOR_SERVICE))
                .toProvider(() -> executorService(SimpleResourceContext.EXECUTOR_SERVICE));

        bind(ExecutorService.class)
                .annotatedWith(named(SimpleIndexContext.EXECUTOR_SERVICE))
                .toProvider(() -> executorService(SimpleIndexContext.EXECUTOR_SERVICE));

        bind(ScheduledExecutorService.class)
                .annotatedWith(named(SCHEDULED_EXECUTOR_SERVICE))
                .toProvider(() -> scheduledExecutorService(SCHEDULED_EXECUTOR_SERVICE));

        expose(Context.class);
        expose(IndexContext.class);
        expose(ResourceContext.class);
        expose(SchedulerContext.class);

    }

    private ExecutorService executorService(final String name) {
        final AtomicInteger threadCount = new AtomicInteger();
        final Logger logger = LoggerFactory.getLogger(SCHEDULED_EXECUTOR_SERVICE);
        return newCachedThreadPool(r -> newThread(r, name, threadCount, logger));
    }

    private ScheduledExecutorService scheduledExecutorService(final String name) {
        final AtomicInteger threadCount = new AtomicInteger();
        final Logger logger = LoggerFactory.getLogger(SCHEDULED_EXECUTOR_SERVICE);
        return newScheduledThreadPool(CORE_POOL_SIZE, r -> newThread(r, name, threadCount, logger));
    }

    private Thread newThread(final Runnable runnable, final String name,
                             final AtomicInteger threadCount, final Logger logger) {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler((t , e) -> logger.error("Scheduler Exception in {}", t, e));
        thread.setName(format("%s - Thread %d", name, threadCount.incrementAndGet()));
        return thread;
    }

}
