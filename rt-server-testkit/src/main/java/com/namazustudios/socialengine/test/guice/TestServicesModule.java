package com.namazustudios.socialengine.test.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.util.Deque;

public class TestServicesModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(SimpleTaskService.class).asEagerSingleton();
        bind(TaskService.class).to(SimpleTaskService.class);
        bind(Scheduler.class).to(SimpleScheduler.class).asEagerSingleton();
        bind(ResourceLockService.class).to(SimpleResourceLockService.class).asEagerSingleton();
        bind(RetainedHandlerService.class).to(SimpleRetainedHandlerService.class).asEagerSingleton();
        bind(SingleUseHandlerService.class).to(SimpleSingleUseHandlerService.class).asEagerSingleton();
        bind(LoadMonitorService.class).to(SimpleLoadMonitorService.class).asEagerSingleton();
        bind(EventService.class).to(SimpleEventService.class).asEagerSingleton();

        bind(new TypeLiteral<OptimisticLockService<Deque<Path>>>() {})
                .toProvider(() -> new ProxyLockService<>(Deque.class));

        bind(new TypeLiteral<OptimisticLockService<ResourceId>>() {})
                .to(SimpleResourceIdOptimisticLockService.class);

        expose(Scheduler.class);
        expose(RetainedHandlerService.class);
        expose(SingleUseHandlerService.class);
        expose(TaskService.class);
        expose(ResourceLockService.class);
        expose(EventService.class);

    }

}
