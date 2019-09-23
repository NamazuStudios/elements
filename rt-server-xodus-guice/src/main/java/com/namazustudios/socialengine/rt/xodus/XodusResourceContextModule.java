package com.namazustudios.socialengine.rt.xodus;

import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.ResourceContext;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.SimpleResourceContext;
import com.namazustudios.socialengine.rt.provider.CPUCountThreadPoolProvider;

import java.util.concurrent.ExecutorService;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Context.LOCAL;
import static com.namazustudios.socialengine.rt.SimpleResourceContext.EXECUTOR_SERVICE;

public class XodusResourceContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(ResourceContext.class)
            .annotatedWith(named(LOCAL));

        // Xodus Resource Context binds to the simple resource scheduler.

        bind(ResourceContext.class)
            .annotatedWith(named(LOCAL))
            .to(SimpleResourceContext.class)
            .asEagerSingleton();

        bind(ExecutorService.class)
            .annotatedWith(Names.named(EXECUTOR_SERVICE))
            .toProvider(new CPUCountThreadPoolProvider(SimpleResourceContext.class));

    }

}
