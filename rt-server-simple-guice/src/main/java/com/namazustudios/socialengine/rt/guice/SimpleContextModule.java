package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.PersistenceStrategy;
import com.namazustudios.socialengine.rt.SimpleContext;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Context.LOCAL;

public class SimpleContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(PersistenceStrategy.class);
        expose(Context.class).annotatedWith(named(LOCAL));

        // The sub-contexts associated with the main context
        install(new SimpleServicesModule());
        install(new SimpleIndexContextModule());
        install(new SimpleResourceContextModule());
        install(new SimpleSchedulerContextModule());
        install(new SimpleHandlerContextModule());
        install(new SimpleTaskContextModule());

        bind(Context.class).annotatedWith(named(LOCAL)).to(SimpleContext.class).asEagerSingleton();

    }

}
