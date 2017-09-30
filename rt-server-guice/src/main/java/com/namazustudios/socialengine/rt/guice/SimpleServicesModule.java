package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.SimpleScheduler.EXECUTOR_SERVICE;
import static java.lang.String.format;
import static java.util.concurrent.Executors.newScheduledThreadPool;

/**
 * Creates the simple internal
 *
 *
 * Created by patricktwohig on 9/22/15.
 */
public class SimpleServicesModule extends AbstractModule {

    private static final int CORE_POOL_SIZE = 5;

    @Override
    protected void configure() {

        bind(Context.class).to(SimpleContext.class).asEagerSingleton();
        bind(Scheduler.class).to(SimpleScheduler.class).asEagerSingleton();
        bind(LockService.class).to(SimpleLockService.class).asEagerSingleton();
        bind(ResourceService.class).to(SimpleResourceService.class).asEagerSingleton();
        bind(PathLockFactory.class).to(SimplePathLockFactory.class).asEagerSingleton();

        final AtomicInteger threadCount = new AtomicInteger();
        final Logger logger = LoggerFactory.getLogger(EXECUTOR_SERVICE);

        bind(ScheduledExecutorService.class)
            .annotatedWith(named(EXECUTOR_SERVICE))
            .toProvider(() -> newScheduledThreadPool(CORE_POOL_SIZE, r -> {
                    final Thread thread = new Thread(r);
                    thread.setDaemon(true);
                    thread.setUncaughtExceptionHandler((t , e) -> logger.error("Scheduler Exception in {}", t, e));
                    thread.setName(format("%s - Thread %d", EXECUTOR_SERVICE, threadCount.incrementAndGet()));
                    return thread;
                }));

    }

}
