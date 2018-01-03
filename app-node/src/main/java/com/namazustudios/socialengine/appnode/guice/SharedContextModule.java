package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.IndexContext;
import com.namazustudios.socialengine.rt.SchedulerContext;
import com.namazustudios.socialengine.rt.SimpleSchedulerContext;

public class SharedContextModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SchedulerContext.class).to(SimpleSchedulerContext.class).asEagerSingleton();
    }

}
