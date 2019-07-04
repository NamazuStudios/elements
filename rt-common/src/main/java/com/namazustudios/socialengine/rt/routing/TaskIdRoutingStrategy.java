package com.namazustudios.socialengine.rt.routing;

import com.namazustudios.socialengine.rt.remote.*;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class TaskIdRoutingStrategy implements RoutingStrategy {

    @Override
    public Future<Object> invokeFuture(
            final List<Object> address,
            final RemoteInvokerRegistry remoteInvokerRegistry,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return null;
    }

    @Override
    public Void invokeAsync(
            final List<Object> address,
            final RemoteInvokerRegistry remoteInvokerRegistry,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return null;
    }

    @Override
    public Object invokeSync(
            final List<Object> address,
            final RemoteInvokerRegistry remoteInvokerRegistry,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception {
        return null;
    }

}
