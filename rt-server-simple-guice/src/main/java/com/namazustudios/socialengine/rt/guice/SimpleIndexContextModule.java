package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.IndexContext;
import com.namazustudios.socialengine.rt.SimpleIndexContext;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Context.LOCAL;

public class SimpleIndexContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(IndexContext.class).annotatedWith(named(LOCAL));

        bind(IndexContext.class)
            .annotatedWith(named(LOCAL))
            .to(SimpleIndexContext.class)
            .asEagerSingleton();

    }

}
