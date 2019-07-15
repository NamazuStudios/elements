package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.IocResolver;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class StandardRemoteInvocationDispatcher implements RemoteInvocationDispatcher {

    private IocResolver iocResolver;

    @Override
    public Future<Object> invokeFuture(final Route route,
                                       final Invocation invocation,
                                       final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                       final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final RoutingStrategy routingStrategy = getRoutingStrategy(route);

        return routingStrategy.invokeFuture(
            route.getAddress(),
            invocation,
            asyncInvocationResultConsumerList,
            asyncInvocationErrorConsumer);

    }

    @Override
    public Void invokeAsync(final Route route,
                            final Invocation invocation,
                            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final RoutingStrategy routingStrategy = getRoutingStrategy(route);

        return routingStrategy.invokeAsync(
            route.getAddress(),
            invocation,
            asyncInvocationResultConsumerList,
            asyncInvocationErrorConsumer);

    }

    @Override
    public Object invokeSync(final Route route,
                             final Invocation invocation,
                             final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                             final InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception {

        final RoutingStrategy routingStrategy = getRoutingStrategy(route);

        return routingStrategy.invokeSync(
            route.getAddress(),
            invocation,
            asyncInvocationResultConsumerList,
            asyncInvocationErrorConsumer);

    }

    public RoutingStrategy getRoutingStrategy(final Route route) {

        final Class<? extends RoutingStrategy> cls = route.getRoutingStrategyType();
        final String name = route.getRoutingStrategyName();

        return name == null || name.isEmpty() ?
            getIocResolver().inject(cls) :
            getIocResolver().inject(cls, name);

    }

    public IocResolver getIocResolver() {
        return iocResolver;
    }

    @Inject
    public void setIocResolver(IocResolver iocResolver) {
        this.iocResolver = iocResolver;
    }

}
