package com.namazustudios.socialengine.rt.guice;

import com.google.inject.PrivateModule;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.IndexContext;
import com.namazustudios.socialengine.rt.SimpleIndexContext;
import com.namazustudios.socialengine.rt.provider.CPUCountThreadPoolProvider;

import java.util.concurrent.ExecutorService;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Context.LOCAL;
import static com.namazustudios.socialengine.rt.SimpleIndexContext.EXECUTOR_SERVICE;

public class SimpleIndexContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(IndexContext.class).annotatedWith(named(LOCAL));

        bind(IndexContext.class)
            .annotatedWith(named(LOCAL))
            .to(SimpleIndexContext.class)
            .asEagerSingleton();

        bind(ExecutorService.class)
            .annotatedWith(named(EXECUTOR_SERVICE))
            .toProvider(new CPUCountThreadPoolProvider(SimpleIndexContext.class));

    }

}
