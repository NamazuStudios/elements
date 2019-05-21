package com.namazustudios.socialengine.rt.remote;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface RoutingStrategy {

    Future<Object> invokeFuture(List<Object> address,
                                RemoteInvokerRegistry remoteInvokerRegistry,
                                Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                InvocationErrorConsumer asyncInvocationErrorConsumer);

    Void invokeAsync(List<Object> address,
                     RemoteInvokerRegistry remoteInvokerRegistry,
                     Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                     InvocationErrorConsumer asyncInvocationErrorConsumer);

    Object invokeSync(List<Object> address,
                      RemoteInvokerRegistry remoteInvokerRegistry,
                      Invocation invocation,
                      List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                      InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception;

    /**
     * The defaul {@link RoutingStrategy} which simply selects a {@link RemoteInvoker} from the
     * {@link RemoteInvokerRegistry} using {@link RemoteInvokerRegistry#getAnyRemoteInvoker()} and sends the
     * {@link Invocation} there.
     */
    class DefaultRoutingStrategy implements RoutingStrategy {

        @Override
        public Future<Object> invokeFuture(
                List<Object> address,
                RemoteInvokerRegistry remoteInvokerRegistry,
                Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                InvocationErrorConsumer asyncInvocationErrorConsumer) {
            return remoteInvokerRegistry.getAnyRemoteInvoker().invokeFuture(
                invocation,
                    asyncInvocationResultConsumerList,
                    asyncInvocationErrorConsumer);
        }

        @Override
        public Void invokeAsync(List<Object> address,
                                RemoteInvokerRegistry remoteInvokerRegistry,
                                Invocation invocation,
                                List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                InvocationErrorConsumer asyncInvocationErrorConsumer) {
            return remoteInvokerRegistry.getAnyRemoteInvoker().invokeAsync(
                    invocation,
                    asyncInvocationResultConsumerList,
                    asyncInvocationErrorConsumer);
        }

        @Override
        public Object invokeSync(List<Object> address,
                                 RemoteInvokerRegistry remoteInvokerRegistry,
                                 Invocation invocation,
                                 List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                 InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception {
            return remoteInvokerRegistry.getAnyRemoteInvoker().invokeSync(
                    invocation,
                    asyncInvocationResultConsumerList,
                    asyncInvocationErrorConsumer);
        }

    }

}
