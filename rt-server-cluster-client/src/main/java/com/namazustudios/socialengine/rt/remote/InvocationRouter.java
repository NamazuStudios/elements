package com.namazustudios.socialengine.rt.remote;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import com.namazustudios.socialengine.rt.RoutingAddressProvider;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;
import com.namazustudios.socialengine.rt.RoutingStrategy;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker.InvocationErrorConsumer;

public interface InvocationRouter {
    /**
     * Takes in an invocation and routes it to the correct destination (i.e. the {@link RemoteInvoker} matching the given address).
     *
     * Note: any errors that occur within this method will be submitted to the provided {@link InvocationErrorConsumer}.
     *
     * @param routingStrategy the routing strategy for the remotely-invoked method as defined in the {@link RemotelyInvokable}
     * @param routingAddressProvider the {@link RoutingAddressProvider} param passed into the invocation.
     * @param invocation
     * @param asyncInvocationResultConsumerList
     * @param asyncInvocationErrorConsumer
     * @return
     */
    Future<Object> performInvocation(RoutingStrategy routingStrategy,
                                     RoutingAddressProvider routingAddressProvider,
                                     Invocation invocation,
                                     List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                     InvocationErrorConsumer asyncInvocationErrorConsumer);
}