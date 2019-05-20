package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.exception.InternalException;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface RoutingStrategy {

    Future<Object> invokeFuture(Object address,
                                RemoteInvokerRegistry remoteInvokerRegistry,
                                Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                InvocationErrorConsumer asyncInvocationErrorConsumer);

    Void invokeAsync(Object address,
                     RemoteInvokerRegistry remoteInvokerRegistry,
                     Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                     InvocationErrorConsumer asyncInvocationErrorConsumer);

    Object invokeSync(Object address,
                      RemoteInvokerRegistry remoteInvokerRegistry,
                      Invocation invocation,
                      List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                      InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception;

    class DefaultRoutingStrategy implements RoutingStrategy {

        @Override
        public Future<Object> invokeFuture(
                Object address,
                RemoteInvokerRegistry remoteInvokerRegistry,
                Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                InvocationErrorConsumer asyncInvocationErrorConsumer) {
            return remoteInvokerRegistry.getAnyRemoteInvoker().invokeFuture(
                invocation,
                    asyncInvocationResultConsumerList,
                    asyncInvocationErrorConsumer);
        }

        @Override
        public Void invokeAsync(Object address,
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
        public Object invokeSync(Object address,
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
