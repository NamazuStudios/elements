package com.namazustudios.socialengine.rt.remote.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.remote.ClusterContext;
import com.namazustudios.socialengine.rt.remote.RemoteProxyProvider;

import static com.google.inject.name.Names.named;

public class ClusterContextModule extends PrivateModule {

    private Runnable bindContextAction = () -> bind(Context.class)
            .to(ClusterContext.class)
            .asEagerSingleton();

    @Override
    protected void configure() {

        expose(Context.class);

        bindContextAction.run();

        bind(IndexContext.class)
                .toProvider(new RemoteProxyProvider<>(IndexContext.class))
            .asEagerSingleton();

        bind(ResourceContext.class)
                .toProvider(new RemoteProxyProvider<>(ResourceContext.class))
            .asEagerSingleton();

        bind(SchedulerContext.class)
                .toProvider(new RemoteProxyProvider<>(SchedulerContext.class))
            .asEagerSingleton();

        bind(HandlerContext.class)
                .toProvider(new RemoteProxyProvider<>(HandlerContext.class))
            .asEagerSingleton();

    }

    /**
     * Specifies the {@link javax.inject.Named} value for the bound {@link Context}.  The context is left unnamed if
     * this is not specified.
     *
     * @param contextName the {@link Context} name
     * @return this instance
     */
    public ClusterContextModule withContextNamed(final String contextName) {
        bindContextAction = () -> bind(Context.class)
                .annotatedWith(named(contextName))
                .to(ClusterContext.class)
            .asEagerSingleton();
        return this;
    }

}
