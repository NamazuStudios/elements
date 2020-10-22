package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.util.Deque;

import static com.namazustudios.socialengine.rt.PersistenceStrategy.getNullPersistence;

/**
 * Creates the simple internal
 * <p>
 * <p>
 * Created by patricktwohig on 9/22/15.
 */
public class SimpleServicesModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(Scheduler.class).to(SimpleScheduler.class).asEagerSingleton();
        bind(ResourceLockService.class).to(SimpleResourceLockService.class).asEagerSingleton();
        bind(ResourceService.class).to(SimpleResourceService.class).asEagerSingleton();
        bind(RetainedHandlerService.class).to(SimpleRetainedHandlerService.class).asEagerSingleton();
        bind(SingleUseHandlerService.class).to(SimpleSingleUseHandlerService.class).asEagerSingleton();
        bind(LoadMonitorService.class).to(SimpleLoadMonitorService.class).asEagerSingleton();
        bind(TaskService.class).to(SimpleTaskService.class);
        bind(PersistenceStrategy.class).toInstance(getNullPersistence());

        bind(new TypeLiteral<OptimisticLockService<Deque<Path>>>() {})
            .toProvider(() -> new ProxyLockService<>(Deque.class));

        bind(new TypeLiteral<OptimisticLockService<ResourceId>>() {})
            .to(SimpleResourceIdOptimisticLockService.class);

        expose(Scheduler.class);
        expose(ResourceService.class);
        expose(RetainedHandlerService.class);
        expose(SingleUseHandlerService.class);
        expose(PersistenceStrategy.class);
        expose(TaskService.class);

    }

}
