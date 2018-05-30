package com.namazustudios.socialengine.rt.xodus;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.provider.CachedThreadPoolProvider;
import com.namazustudios.socialengine.rt.provider.ScheduledExecutorServiceProvider;

import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Constants.SCHEDULER_THREADS;
import static com.namazustudios.socialengine.rt.SimpleScheduler.DISPATCHER_EXECUTOR_SERVICE;
import static com.namazustudios.socialengine.rt.SimpleScheduler.SCHEDULED_EXECUTOR_SERVICE;

public class XodusServicesModule extends PrivateModule {

    private Runnable bindSchedulerThreads = () -> {};

    public XodusServicesModule withSchedulerThreads(final int threads) {
        bindSchedulerThreads = () -> bind(Integer.class)
            .annotatedWith(named(SCHEDULER_THREADS))
            .toInstance(threads);
        return this;
    }

    @Override
    protected void configure() {

        bindSchedulerThreads.run();

        bind(Scheduler.class).to(SimpleScheduler.class).asEagerSingleton();
        bind(ResourceLockService.class).to(SimpleResourceLockService.class).asEagerSingleton();

        bind(ResourceService.class).to(XodusResourceService.class);
        bind(XodusResourceService.class).asEagerSingleton();

        bind(ScheduledExecutorService.class)
            .annotatedWith(named(SCHEDULED_EXECUTOR_SERVICE))
            .toProvider(ScheduledExecutorServiceProvider.class);

        bind(ExecutorService.class)
            .annotatedWith(named(DISPATCHER_EXECUTOR_SERVICE))
            .toProvider(new CachedThreadPoolProvider(SimpleScheduler.class, "dispatch"));

        expose(Scheduler.class);
        expose(ResourceService.class);
//        expose(ResourceLockService.class);

    }

}
