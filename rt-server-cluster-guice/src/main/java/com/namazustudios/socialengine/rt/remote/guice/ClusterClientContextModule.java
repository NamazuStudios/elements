package com.namazustudios.socialengine.rt.remote.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.remote.ClusterClientContext;
import com.namazustudios.socialengine.rt.remote.RemoteProxyProvider;

import static com.google.inject.name.Names.named;

public class ClusterClientContextModule extends PrivateModule {

    private Runnable bindContextAction = () -> bind(Context.class)
            .to(ClusterClientContext.class)
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

//        bind(RemoteInvokerRegistry.class)
//            .to(JeroMQRemoteInvokerRegistry.class)
//            .asEagerSingleton();

//        bind(InstanceConnectionMonitorService.class)
//            .to(JeroMQInstanceConnectionMonitorService.class)
//            .asEagerSingleton();
//
//        bind(InstanceResourceMonitorService.class)
//            .to(JeroMQInstanceResourceMonitorService.class)
//            .asEagerSingleton();

// TODO Fix this
//        bind(RemoteAddressRegistry.class)
//                .to(JeroMQRemoteAddressRegistry.class)
//                .asEagerSingleton();

    }

    /**
     * Specifies the {@link javax.inject.Named} value for the bound {@link Context}.  The context is left unnamed if
     * this is not specified.
     *
     * @param contextName the {@link Context} name
     * @return this instance
     */
    public ClusterClientContextModule withContextNamed(final String contextName) {
        bindContextAction = () -> bind(Context.class)
                .annotatedWith(named(contextName))
                .to(ClusterClientContext.class)
                .asEagerSingleton();
        return this;
    }

}
