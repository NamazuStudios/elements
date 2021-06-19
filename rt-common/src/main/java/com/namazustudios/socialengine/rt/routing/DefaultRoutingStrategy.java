package com.namazustudios.socialengine.rt.routing;

import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * The default {@link RoutingStrategy} which simply selects a {@link RemoteInvoker} from the
 * {@link RemoteInvokerRegistry} using {@link RemoteInvokerRegistry#getBestRemoteInvoker(ApplicationId)} and sends the
 * {@link Invocation} there.
 */
public class DefaultRoutingStrategy implements RoutingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(com.namazustudios.socialengine.rt.routing.DefaultRoutingStrategy.class);

    private ApplicationId applicationId;

    private RemoteInvokerRegistry remoteInvokerRegistry;

    @Override
    public Future<Object> invokeFuture(
            final List<Object> address,
            final Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        if (!address.isEmpty()) logger.warn("Ignoring routing address {}", address);

        return getRemoteInvokerRegistry().getBestRemoteInvoker(getApplicationId()).invokeFuture(
            invocation,
            asyncInvocationResultConsumerList,
            asyncInvocationErrorConsumer);
    }

    @Override
    public AsyncOperation invokeAsync(
            final List<Object> address,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        if (!address.isEmpty()) logger.warn("Ignoring routing address {}", address);

        return getRemoteInvokerRegistry().getBestRemoteInvoker(getApplicationId()).invokeAsync(
            invocation,
            asyncInvocationResultConsumerList,
            asyncInvocationErrorConsumer);

    }

    @Override
    public Object invokeSync(final List<Object> address,
                             final Invocation invocation,
                             final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                             final InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception {

        if (!address.isEmpty()) logger.warn("Ignoring routing address {}", address);

        return getRemoteInvokerRegistry().getBestRemoteInvoker(getApplicationId()).invokeSync(
            invocation,
            asyncInvocationResultConsumerList,
            asyncInvocationErrorConsumer);
    }

    public RemoteInvokerRegistry getRemoteInvokerRegistry() {
        return remoteInvokerRegistry;
    }

    @Inject
    public void setRemoteInvokerRegistry(RemoteInvokerRegistry remoteInvokerRegistry) {
        this.remoteInvokerRegistry = remoteInvokerRegistry;
    }

    public ApplicationId getApplicationId() {
        return applicationId;
    }

    @Inject
    public void setApplicationId(ApplicationId applicationId) {
        this.applicationId = applicationId;
    }

}
