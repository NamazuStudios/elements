package com.namazustudios.socialengine.rt.xodus;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.ResourceAcquisition;
import com.namazustudios.socialengine.rt.SimpleContext;
import com.namazustudios.socialengine.rt.guice.*;

import static com.google.inject.name.Names.named;

public class XodusContextModule extends PrivateModule {

    private Runnable bindContextAction = () -> {
        expose(Context.class);
        bind(Context.class).to(SimpleContext.class).asEagerSingleton();
    };

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

        // The main context for the application
        bindContextAction.run();

        // Configures all services to be backed by Xodus.  Many of them are the Simple services, but this installs
        // just the Xodus required
        install(new XodusServicesModule());

        // Some contexts handle the connections with Xodus
        install(new XodusSchedulerContextModule());
        install(new XodusResourceContextModule());

        // The remaining contexts are fine as their simple equivalents.
        install(new SimpleIndexContextModule());
        install(new SimpleHandlerContextModule());

        expose(ResourceAcquisition.class);

    }

}

