package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.EventContext;
import com.namazustudios.socialengine.rt.SimpleEventContext;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Context.LOCAL;

public class SimpleEventContextModule extends PrivateModule {
    @Override
    protected void configure() {

        expose(EventContext.class)
            .annotatedWith(named(LOCAL));

        bind(EventContext.class)
            .annotatedWith(named(LOCAL))
            .to(SimpleEventContext.class)
            .asEagerSingleton();

    }

}
