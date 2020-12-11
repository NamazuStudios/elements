package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.PersistenceStrategy;
import com.namazustudios.socialengine.rt.SimpleContext;

import static com.google.inject.name.Names.named;

public class SimpleContextModule extends PrivateModule {

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
    public SimpleContextModule withContextNamed(final String contextName) {

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

        // The sub-contexts associated with the main context
        install(new SimpleServicesModule());
        install(new SimpleIndexContextModule());
        install(new SimpleResourceContextModule());
        install(new SimpleSchedulerContextModule());
        install(new SimpleHandlerContextModule());
        install(new SimpleTaskContextModule());
        install(new SimpleEventContextModule());

        expose(PersistenceStrategy.class);

    }

}
