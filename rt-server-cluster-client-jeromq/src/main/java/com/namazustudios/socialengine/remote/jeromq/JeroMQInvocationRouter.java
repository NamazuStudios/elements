package com.namazustudios.socialengine.remote.jeromq;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import com.namazustudios.socialengine.rt.RoutingAddressProvider;
import com.namazustudios.socialengine.rt.RoutingStrategy;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker.InvocationErrorConsumer;

import javax.inject.Inject;

public class JeroMQInvocationRouter implements InvocationRouter {
    private RemoteInvokerRegistry remoteInvokerRegistry;

    public Future<Object> performInvocation(RoutingStrategy routingStrategy,
                                            RoutingAddressProvider routingAddressProvider,
                                            Invocation invocation,
                                            List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                            InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return null;
    }

    public RemoteInvokerRegistry getRemoteInvokerRegistry() {
        return remoteInvokerRegistry;
    }

    @Inject
    public void setRemoteInvokerRegistry(RemoteInvokerRegistry remoteInvokerRegistry) {
        this.remoteInvokerRegistry = remoteInvokerRegistry;
    }
}