package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.SchedulerContext;
import com.namazustudios.socialengine.rt.SimpleSchedulerContext;

public class SimpleSchedulerContextModule extends PrivateModule {

    @Override
    protected void configure() {
        expose(SchedulerContext.class);
        bind(SchedulerContext.class).to(SimpleSchedulerContext.class).asEagerSingleton();
    }

}
