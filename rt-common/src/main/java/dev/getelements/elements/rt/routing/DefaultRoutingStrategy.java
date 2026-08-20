package dev.getelements.elements.rt.routing;

import dev.getelements.elements.sdk.cluster.id.DeploymentId;
import dev.getelements.elements.rt.remote.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * The default {@link RoutingStrategy} which simply selects a {@link RemoteInvoker} from the
 * {@link RemoteInvokerRegistry} using {@link RemoteInvokerRegistry#getBestRemoteInvoker(DeploymentId)} and sends the
 * {@link Invocation} there.
 */
public class DefaultRoutingStrategy implements RoutingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(dev.getelements.elements.rt.routing.DefaultRoutingStrategy.class);

    private DeploymentId deploymentId;

    private RemoteInvokerRegistry remoteInvokerRegistry;

    @Override
    public Future<Object> invokeFuture(
            final List<Object> address,
            final Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        if (!address.isEmpty()) logger.warn("Ignoring routing address {}", address);

        return getRemoteInvokerRegistry().getBestRemoteInvoker(getDeploymentId()).invokeFuture(
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

        return getRemoteInvokerRegistry().getBestRemoteInvoker(getDeploymentId()).invokeAsync(
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

        return getRemoteInvokerRegistry().getBestRemoteInvoker(getDeploymentId()).invokeSync(
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

    public DeploymentId getDeploymentId() {
        return deploymentId;
    }

    @Inject
    public void setDeploymentId(DeploymentId deploymentId) {
        this.deploymentId = deploymentId;
    }

}
