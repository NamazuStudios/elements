package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.IocResolver;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class StandardRemoteInvocationDispatcher implements RemoteInvocationDispatcher {

    private IocResolver iocResolver;

    private RemoteInvokerRegistry remoteInvokerRegistry;

    @Override
    public Future<Object> invokeFuture(final Route route,
                                       final Invocation invocation,
                                       final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                       final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final RoutingStrategy routingStrategy = getIocResolver().inject(route.getRoutingStrategyType());

        return routingStrategy.invokeFuture(
            route.getAddress(), getRemoteInvokerRegistry(),
            invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);

    }

    @Override
    public Void invokeAsync(final Route route,
                            final Invocation invocation,
                            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final RoutingStrategy routingStrategy = getIocResolver().inject(route.getRoutingStrategyType());

        return routingStrategy.invokeAsync(
                route.getAddress(), getRemoteInvokerRegistry(),
                invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);

    }

    @Override
    public Object invokeSync(final Route route,
                             final Invocation invocation,
                             final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                             final InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception {

        final RoutingStrategy routingStrategy = getIocResolver().inject(route.getRoutingStrategyType());

        return routingStrategy.invokeSync(
                route.getAddress(), getRemoteInvokerRegistry(),
                invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);

    }

    public IocResolver getIocResolver() {
        return iocResolver;
    }

    @Inject
    public void setIocResolver(IocResolver iocResolver) {
        this.iocResolver = iocResolver;
    }

    public RemoteInvokerRegistry getRemoteInvokerRegistry() {
        return remoteInvokerRegistry;
    }

    @Inject
    public void setRemoteInvokerRegistry(RemoteInvokerRegistry remoteInvokerRegistry) {
        this.remoteInvokerRegistry = remoteInvokerRegistry;
    }

}
