package dev.getelements.elements.sdk.cluster.remote.routing;

import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import dev.getelements.elements.sdk.cluster.remote.AsyncOperation;
import dev.getelements.elements.sdk.cluster.remote.InvocationErrorConsumer;
import dev.getelements.elements.sdk.cluster.remote.RemoteInvoker;
import dev.getelements.elements.sdk.cluster.remote.RemoteInvokerRegistry;
import dev.getelements.elements.sdk.cluster.remote.dto.Invocation;
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * The default {@link RoutingStrategy} which simply selects the best {@link RemoteInvoker} from the
 * {@link RemoteInvokerRegistry}, based on the instance selector derived from the invocation's address, and
 * dispatches the invocation directly to it.
 */
public class DefaultRoutingStrategy implements RoutingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRoutingStrategy.class);

    private ApplicationId applicationId;

    private RemoteInvokerRegistry remoteInvokerRegistry;

    @Override
    public Future<Object> invokeFuture(
            final RemoteInvokerRegistry remoteInvokerRegistry,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final var instanceSelector = invocation
                .address()
                .service()
                .element()
                .instance();

        return remoteInvokerRegistry.getBestRemoteInvoker(instanceSelector).invokeFuture(
            invocation,
            asyncInvocationResultConsumerList,
            asyncInvocationErrorConsumer);

    }

    @Override
    public AsyncOperation invokeAsync(
            final RemoteInvokerRegistry remoteInvokerRegistry,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        final var instanceSelector = invocation
                .address()
                .service()
                .element()
                .instance();

        return remoteInvokerRegistry.getBestRemoteInvoker(instanceSelector).invokeAsync(
            invocation,
            asyncInvocationResultConsumerList,
            asyncInvocationErrorConsumer);

    }

    @Override
    public Object invokeSync(
            final RemoteInvokerRegistry remoteInvokerRegistry,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception {

        final var instanceSelector = invocation
                .address()
                .service()
                .element()
                .instance();

        return remoteInvokerRegistry.getBestRemoteInvoker(instanceSelector).invokeSync(
            invocation,
            asyncInvocationResultConsumerList,
            asyncInvocationErrorConsumer);

    }

}
