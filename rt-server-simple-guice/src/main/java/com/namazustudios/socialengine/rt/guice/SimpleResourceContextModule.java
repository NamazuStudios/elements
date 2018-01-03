package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.ResourceContext;
import com.namazustudios.socialengine.rt.SimpleResourceContext;
import com.namazustudios.socialengine.rt.provider.CachedThreadPoolProvider;

import java.util.concurrent.ExecutorService;

import static com.namazustudios.socialengine.rt.SimpleResourceContext.*;

public class SimpleResourceContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(ResourceContext.class);

        bind(ResourceContext.class).to(SimpleResourceContext.class).asEagerSingleton();

        bind(ExecutorService.class)
            .annotatedWith(Names.named(EXECUTOR_SERVICE))
            .toProvider(new CachedThreadPoolProvider(SimpleResourceContext.class));

    }

}
