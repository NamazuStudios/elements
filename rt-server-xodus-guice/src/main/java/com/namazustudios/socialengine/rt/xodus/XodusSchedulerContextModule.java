package com.namazustudios.socialengine.rt.xodus;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.SchedulerContext;
import com.namazustudios.socialengine.rt.SimpleSchedulerContext;

public class XodusSchedulerContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(SchedulerContext.class);

        // Xodus Scheduler Context depends on a SimpleSchedulerContext to perform its work, it just adds some
        // support on top of it.

        bind(SimpleSchedulerContext.class).asEagerSingleton();
        bind(SchedulerContext.class).to(XodusSchedulerContext.class).asEagerSingleton();

    }

}
