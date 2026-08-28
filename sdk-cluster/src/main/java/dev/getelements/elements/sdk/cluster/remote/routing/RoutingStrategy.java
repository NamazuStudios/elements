package dev.getelements.elements.sdk.cluster.remote.routing;

import dev.getelements.elements.sdk.address.ElementMethodAddress;
import dev.getelements.elements.sdk.cluster.address.RemoteInstanceSelector;
import dev.getelements.elements.sdk.cluster.remote.*;
import dev.getelements.elements.sdk.cluster.remote.dto.Invocation;
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * A {@link RoutingStrategy} relies on the {@link RemoteInvokerRegistry} and address data to route an {@link Invocation}
 * to a specific {@link RemoteInvoker} based on the addressing parameters in the method that is invoked.
 */
public interface RoutingStrategy {

    /**
     * Invokes the method returning a {@link Future<Object>} with the result.
     *
     * Corresponds to {@link RemoteInvoker#invokeFuture(Invocation, List, InvocationErrorConsumer)}.
     *
     * @param invocation the {@link Invocation}
     * @param asyncInvocationResultConsumerList the list of {@link Consumer<InvocationResult>} instances
     * @param asyncInvocationErrorConsumer the {@link InvocationErrorConsumer} to receive the error of the invocation
     *
     * @return the {@link Future<Object>} to handle the return value
     */
    Future<Object> invokeFuture(
            RemoteInvokerRegistry remoteInvokerRegistry,
            Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            InvocationErrorConsumer asyncInvocationErrorConsumer);

    /**
     * Invokes the method asynchronously returning a {@link Void} (ie null) for the value.
     *
     * @param invocation the {@link Invocation}
     * @param asyncInvocationResultConsumerList the list of {@link Consumer<InvocationResult>} instances
     * @param asyncInvocationErrorConsumer the {@link InvocationErrorConsumer} to receive the error of the invocation
     *
     * @return null
     */
    default Void invokeAsyncV(
            final RemoteInvokerRegistry remoteInvokerRegistry,
            final Invocation invocation,
            final List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            final InvocationErrorConsumer asyncInvocationErrorConsumer) {

        invokeAsync(
                remoteInvokerRegistry,
                invocation,
                asyncInvocationResultConsumerList,
                asyncInvocationErrorConsumer
        );

        return null;

    }

    /**
     * Invokes the method returning a {@link AsyncOperation} for the value.
     *
     * @param invocation the {@link Invocation}
     * @param asyncInvocationResultConsumerList the list of {@link Consumer<InvocationResult>} instances
     * @param asyncInvocationErrorConsumer the {@link InvocationErrorConsumer} to receive the error of the invocation
     *
     * @return an instance of {@link AsyncOperation}
     */
    AsyncOperation invokeAsync(
        RemoteInvokerRegistry remoteInvokerRegistry,
        Invocation invocation,
        List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
        InvocationErrorConsumer asyncInvocationErrorConsumer);

    /**
     * Invokes the method, blocking until it returns on the remote end.
     *
     * Corresponds to {@link RemoteInvoker#invokeFuture(Invocation, List, InvocationErrorConsumer)}.
     *
     * @param invocation the {@link Invocation}
     * @param asyncInvocationResultConsumerList the list of {@link Consumer<InvocationResult>} instances
     * @param asyncInvocationErrorConsumer the {@link InvocationErrorConsumer} to receive the error of the invocation
     *
     * @return the return value of the remote invocation
     */
    Object invokeSync(
            RemoteInvokerRegistry remoteInvokerRegistry,
            Invocation invocation,
            List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception;

}

