package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.provider.CachedThreadPoolProvider;
import com.namazustudios.socialengine.rt.provider.ScheduledExecutorServiceProvider;

import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Constants.SCHEDULER_THREADS;
import static com.namazustudios.socialengine.rt.SimpleScheduler.DISPATCHER_EXECUTOR_SERVICE;
import static com.namazustudios.socialengine.rt.SimpleScheduler.SCHEDULED_EXECUTOR_SERVICE;

/**
 * Creates the simple internal
 * <p>
 * <p>
 * Created by patricktwohig on 9/22/15.
 */
public class SimpleServicesModule extends PrivateModule {

    private Runnable bindSchedulerThreads = () -> {};

    /**
     * Specifies the number of scheduler threads.  This number typically can be set low as the actual scheduler threads
     * defer their work to a cached thread pool.  Typically this is set to 1+ the currently availble CPUs
     *
     * @param threads the number of threads
     * @return  this instance
     */
    public SimpleServicesModule withSchedulerThreads(int threads) {
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
        bind(ResourceService.class).to(SimpleResourceService.class).asEagerSingleton();
        bind(RetainedHandlerService.class).to(SimpleRetainedHandlerService.class).asEagerSingleton();
        bind(SingleUseHandlerService.class).to(SimpleSingleUseHandlerService.class).asEagerSingleton();
        bind(ResourceAcquisition.class).to(NullResourceAcquisition.class).asEagerSingleton();
        bind(LoadMonitorService.class).to(SimpleLoadMonitorService.class).asEagerSingleton();

        bind(new TypeLiteral<OptimisticLockService<Deque<Path>>>() {})
            .toProvider(() -> new ProxyLockService<>(Deque.class));

        bind(new TypeLiteral<OptimisticLockService<ResourceId>>() {})
            .to(SimpleResourceIdOptimisticLockService.class);

        bind(ScheduledExecutorService.class)
            .annotatedWith(named(SCHEDULED_EXECUTOR_SERVICE))
            .toProvider(ScheduledExecutorServiceProvider.class);

        bind(ExecutorService.class)
            .annotatedWith(named(DISPATCHER_EXECUTOR_SERVICE))
            .toProvider(new CachedThreadPoolProvider(SimpleScheduler.class, "dispatch"));

        expose(Scheduler.class);
        expose(ResourceService.class);
        expose(RetainedHandlerService.class);
        expose(SingleUseHandlerService.class);
        expose(ResourceAcquisition.class);

    }

}
