package com.namazustudios.socialengine.rt.xodus;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.guice.*;

import java.util.concurrent.TimeUnit;

import static com.google.inject.name.Names.named;

public class XodusContextModule extends PrivateModule {

    private Runnable bindContextAction = () -> {
        expose(Context.class);
        bind(Context.class).to(SimpleContext.class).asEagerSingleton();
    };

    private Runnable handlerTimeoutBindAction = () -> {};

    private final XodusServicesModule xodusServicesModule = new XodusServicesModule();

    private final SimpleHandlerContextModule simpleHandlerContextModule = new SimpleHandlerContextModule();

    /**
     * Specifies the {@link javax.inject.Named} value for the bound {@link Context}.  The context is left unnamed if
     * this is not specified.
     *
     * @param contextName the {@link Context} name
     * @return this instance
     */
    public XodusContextModule withContextNamed(final String contextName) {

        bindContextAction = () -> {
            expose(Context.class).annotatedWith(named(contextName));
            bind(Context.class).annotatedWith(named(contextName)).to(SimpleContext.class).asEagerSingleton();
        };

        return this;

    }

    @Override
    protected void configure() {

        bindContextAction.run();
        handlerTimeoutBindAction.run();

        // Configures all services to be backed by Xodus.  Many of them are the Simple services, but this installs
        // just the Xodus required
        install(xodusServicesModule);

        // Some contexts handle the connections with Xodus
        install(new XodusSchedulerContextModule());
        install(new XodusResourceContextModule());

        // The remaining contexts are fine as their simple equivalents.
        install(new SimpleIndexContextModule());
        install(simpleHandlerContextModule);
        install(new SimpleInstanceMetadataContextModule());

        expose(IndexContext.class);
        expose(ResourceContext.class);
        expose(HandlerContext.class);
        expose(SchedulerContext.class);
        expose(ResourceAcquisition.class);

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

