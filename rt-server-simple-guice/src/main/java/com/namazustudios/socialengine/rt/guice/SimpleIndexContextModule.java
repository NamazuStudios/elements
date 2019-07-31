package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.IndexContext;
import com.namazustudios.socialengine.rt.SimpleIndexContext;
import com.namazustudios.socialengine.rt.provider.CPUCountThreadPoolProvider;

import java.util.concurrent.ExecutorService;

import static com.namazustudios.socialengine.rt.SimpleIndexContext.EXECUTOR_SERVICE;

public class SimpleIndexContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(IndexContext.class);

        bind(IndexContext.class).to(SimpleIndexContext.class).asEagerSingleton();

        bind(ExecutorService.class)
            .annotatedWith(Names.named(EXECUTOR_SERVICE))
            .toProvider(new CPUCountThreadPoolProvider(SimpleIndexContext.class));

    }

}
