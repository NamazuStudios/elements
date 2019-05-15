package com.namazustudios.socialengine.rt.remote;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker.InvocationErrorConsumer;

public interface InvocationRouter {
    /**
     * Takes in an invocation and routes it to the correct destination (i.e. the RemoteInvoker matching the given address).
     *
     * Note: any errors that occur within this method will be submitted to the provided {@link InvocationErrorConsumer}.
     *
     * @param address the node destination address String, or null if the invocation is not meant to be addressed.
     * @param invocation
     * @param asyncInvocationResultConsumerList
     * @param asyncInvocationErrorConsumer
     * @return
     */
    Future<Object> performInvocation(String address,
                                     Set<RoutingStrategy> routingStrategies,
                                     Invocation invocation,
                                     List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                     InvocationErrorConsumer asyncInvocationErrorConsumer);
}