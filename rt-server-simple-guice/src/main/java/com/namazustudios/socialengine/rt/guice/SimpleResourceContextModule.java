package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.ResourceContext;
import com.namazustudios.socialengine.rt.SimpleResourceContext;
import com.namazustudios.socialengine.rt.remote.provider.CPUCountThreadPoolProvider;

import java.util.concurrent.ExecutorService;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Context.LOCAL;
import static com.namazustudios.socialengine.rt.SimpleResourceContext.*;

public class SimpleResourceContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(ResourceContext.class)
            .annotatedWith(named(LOCAL));

        bind(ResourceContext.class)
            .annotatedWith(named(LOCAL))
            .to(SimpleResourceContext.class)
            .asEagerSingleton();

        bind(ExecutorService.class)
            .annotatedWith(named(EXECUTOR_SERVICE))
            .toProvider(new CPUCountThreadPoolProvider(SimpleResourceContext.class));

    }

}
