package com.namazustudios.socialengine.rt.remote;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * A {@link RoutingStrategy} relies on the {@link RemoteInvokerRegistry} and address data to route an {@link Invocation}
 * to a specific {@link }
 */
public interface RoutingStrategy {

    Future<Object> invokeFuture(
            List<Object> address,
            Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            InvocationErrorConsumer asyncInvocationErrorConsumer);

    Void invokeAsync(
            List<Object> address,
            Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            InvocationErrorConsumer asyncInvocationErrorConsumer);

    Object invokeSync(
            List<Object> address,
            Invocation invocation,
            List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception;

}
