package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.SchedulerContext;
import com.namazustudios.socialengine.rt.SimpleSchedulerContext;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Constants.SCHEDULER_THREADS;
import static com.namazustudios.socialengine.rt.Context.LOCAL;

public class SimpleSchedulerContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(SchedulerContext.class)
            .annotatedWith(named(LOCAL));

        bind(SchedulerContext.class)
            .annotatedWith(named(LOCAL))
            .to(SimpleSchedulerContext.class)
            .asEagerSingleton();

    }

}
