package com.namazustudios.socialengine.rt.remote.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.remote.ClusterContext;
import com.namazustudios.socialengine.rt.remote.RemoteProxyProvider;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.Context.REMOTE;

public class ClusterContextModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(Context.class)
            .annotatedWith(named(REMOTE));

        bind(Context.class)
            .annotatedWith(named(REMOTE))
            .to(ClusterContext.class)
            .asEagerSingleton();

        bind(IndexContext.class)
            .annotatedWith(named(REMOTE))
            .toProvider(new RemoteProxyProvider<>(IndexContext.class, REMOTE))
            .asEagerSingleton();

        bind(ResourceContext.class)
            .annotatedWith(named(REMOTE))
            .toProvider(new RemoteProxyProvider<>(ResourceContext.class, REMOTE))
            .asEagerSingleton();

        bind(SchedulerContext.class)
            .annotatedWith(named(REMOTE))
            .toProvider(new RemoteProxyProvider<>(SchedulerContext.class, REMOTE))
            .asEagerSingleton();

        bind(HandlerContext.class)
            .annotatedWith(named(REMOTE))
            .toProvider(new RemoteProxyProvider<>(HandlerContext.class, REMOTE))
            .asEagerSingleton();

        bind(TaskContext.class)
            .annotatedWith(named(REMOTE))
            .toProvider(new RemoteProxyProvider<>(TaskContext.class, REMOTE))
            .asEagerSingleton();

        bind(ManifestContext.class)
            .annotatedWith(named(REMOTE))
            .toProvider(new RemoteProxyProvider<>(ManifestContext.class, REMOTE))
            .asEagerSingleton();

        bind(EventContext.class)
            .annotatedWith(named(REMOTE))
            .toProvider(new RemoteProxyProvider<>(EventContext.class, REMOTE))
            .asEagerSingleton();

    }

}
