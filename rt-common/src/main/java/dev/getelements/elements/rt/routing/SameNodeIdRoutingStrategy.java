package dev.getelements.elements.rt.routing;

import dev.getelements.elements.rt.id.HasNodeId;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.remote.*;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static dev.getelements.elements.rt.routing.RoutingUtility.reduceAddressToSingleNodeId;

/**
 * A {@link RoutingStrategy} which ensures that accepts all address components are instances of {@link HasNodeId} and
 * that all {@link NodeId} instances point to the same place.  Instances of {@link HasNodeId} which do not specify the
 * {@link NodeId} will be ignored and the call dispatched to the specific {@link NodeId} that it determines or an
 * exception will be thrown otherwise.
 *
 * If no {@link NodeId}s can be determined from the address, then an exception is thrown.  However, this will ensure
 * that the call will be routed to a specific node with the determined {@link NodeId}.
 */
public class SameNodeIdRoutingStrategy implements RoutingStrategy {

    private RemoteInvokerRegistry remoteInvokerRegistry;

    @Override
    public Future<Object> invokeFuture(
            final List<Object> address,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {
        final NodeId nodeId = reduceAddressToSingleNodeId(address);
        final RemoteInvoker remoteInvoker = getRemoteInvokerRegistry().getRemoteInvoker(nodeId);
        return remoteInvoker.invokeFuture(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
    }

    @Override
    public AsyncOperation invokeAsync(
            final List<Object> address,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {
        final NodeId nodeId = reduceAddressToSingleNodeId(address);
        final RemoteInvoker remoteInvoker = getRemoteInvokerRegistry().getRemoteInvoker(nodeId);
        return remoteInvoker.invokeAsync(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
    }

    @Override
    public Object invokeSync(
            final List<Object> address,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception {
        final NodeId nodeId = reduceAddressToSingleNodeId(address);
        final RemoteInvoker remoteInvoker = getRemoteInvokerRegistry().getRemoteInvoker(nodeId);
        return remoteInvoker.invokeSync(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
    }

    public RemoteInvokerRegistry getRemoteInvokerRegistry() {
        return remoteInvokerRegistry;
    }

    @Inject
    public void setRemoteInvokerRegistry(RemoteInvokerRegistry remoteInvokerRegistry) {
        this.remoteInvokerRegistry = remoteInvokerRegistry;
    }

}
