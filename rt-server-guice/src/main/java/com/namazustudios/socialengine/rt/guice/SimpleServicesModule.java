package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.*;

/**
 * Creates the simple internal
 *
 *
 * Created by patricktwohig on 9/22/15.
 */
public class SimpleServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Scheduler.class).to(SimpleScheduler.class).asEagerSingleton();
        bind(LockService.class).to(SimpleLockService.class).asEagerSingleton();
        bind(ResourceService.class).to(SimpleResourceService.class).asEagerSingleton();
    }

}
