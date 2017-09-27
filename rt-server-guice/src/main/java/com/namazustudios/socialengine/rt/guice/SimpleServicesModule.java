package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.*;

import java.util.concurrent.ScheduledExecutorService;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.SimpleScheduler.EXECUTOR_SERVICE;
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

        bind(ScheduledExecutorService.class)
            .annotatedWith(named(EXECUTOR_SERVICE))
            .toProvider(() -> newScheduledThreadPool(CORE_POOL_SIZE));

    }

}
