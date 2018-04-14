package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

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

    private static final int CORE_POOL_SIZE = 500;

    @Override
    protected void configure() {


        // The actual underlying services
        bind(Scheduler.class).to(SimpleScheduler.class).asEagerSingleton();
        bind(ResourceLockService.class).to(SimpleResourceLockService.class).asEagerSingleton();
        bind(ResourceService.class).to(SimpleResourceService.class).asEagerSingleton();

        bind(new TypeLiteral<OptimisticLockService<Deque<Path>>>() {})
                .toProvider(() -> new ProxyLockService<>(Deque.class));

        bind(new TypeLiteral<OptimisticLockService<ResourceId>>() {})
                .to(SimpleResourceIdOptimisticLockService.class);

        bind(ScheduledExecutorService.class)
                .annotatedWith(Names.named(SCHEDULED_EXECUTOR_SERVICE))
                .toProvider(() -> scheduledExecutorService(SCHEDULED_EXECUTOR_SERVICE));

        expose(Scheduler.class);
        expose(ResourceService.class);

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
        thread.setUncaughtExceptionHandler((t, e) -> logger.error("Scheduler Exception in {}", t, e));
        thread.setName(format("%s - Thread %d", name, threadCount.incrementAndGet()));
        return thread;
    }

}
