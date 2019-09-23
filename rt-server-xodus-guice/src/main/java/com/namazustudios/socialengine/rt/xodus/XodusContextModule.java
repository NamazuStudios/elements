package com.namazustudios.socialengine.rt.xodus;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.guice.SimpleHandlerContextModule;
import com.namazustudios.socialengine.rt.guice.SimpleIndexContextModule;
import com.namazustudios.socialengine.rt.guice.SimpleServicesModule;
import com.namazustudios.socialengine.rt.guice.SimpleTaskContextModule;

import java.util.concurrent.TimeUnit;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Context.LOCAL;

public class XodusContextModule extends PrivateModule {

    private Runnable handlerTimeoutBindAction = () -> {};

    private final XodusServicesModule xodusServicesModule = new XodusServicesModule();

    private final SimpleHandlerContextModule simpleHandlerContextModule = new SimpleHandlerContextModule();


    @Override
    protected void configure() {

        handlerTimeoutBindAction.run();

        // Binds the SimpleContext
        bind(Context.class).annotatedWith(named(LOCAL)).to(SimpleContext.class).asEagerSingleton();

        // Configures all services to be backed by Xodus.  Many of them are the Simple services, but this installs
        // just the Xodus required
        install(xodusServicesModule);

        // Some contexts handle the connections with Xodus
        install(new XodusSchedulerContextModule());
        install(new XodusResourceContextModule());

        // The remaining contexts are fine as their simple equivalents.
        install(new SimpleTaskContextModule());
        install(new SimpleIndexContextModule());
        install(simpleHandlerContextModule);

        // Exposes everything
        expose(Context.class).annotatedWith(named(LOCAL));
        expose(IndexContext.class).annotatedWith(named(LOCAL));
        expose(ResourceContext.class).annotatedWith(named(LOCAL));
        expose(HandlerContext.class).annotatedWith(named(LOCAL));
        expose(SchedulerContext.class).annotatedWith(named(LOCAL));
        expose(TaskContext.class).annotatedWith(named(LOCAL));
        expose(PersistenceStrategy.class);

    }

    /**
     * {@see {@link SimpleHandlerContextModule#withTimeout(long, TimeUnit)}}
     *
     * @param duration the duration
     * @param sourceUnits the source units of measure
     * @return this instance
     */
    public XodusContextModule withHandlerTimeout(final long duration, final TimeUnit sourceUnits) {
        handlerTimeoutBindAction = () -> simpleHandlerContextModule.withTimeout(duration, sourceUnits);
        return this;
    }

    /**
     * {@see {@link SimpleServicesModule#withSchedulerThreads(int)}}
     *
     * @param schedulerThreads scheduler threads
     * @return this instance
     */
    public XodusContextModule withSchedulerThreads(final int schedulerThreads) {
        xodusServicesModule.withSchedulerThreads(schedulerThreads);
        return this;
    }

}

