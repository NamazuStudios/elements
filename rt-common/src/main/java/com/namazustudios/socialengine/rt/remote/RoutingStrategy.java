package com.namazustudios.socialengine.rt.remote;

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
     * @param address the address
     * @param invocation the {@link Invocation}
     * @param asyncInvocationResultConsumerList the list of {@link Consumer<InvocationResult>} instances
     * @param asyncInvocationErrorConsumer the {@link InvocationErrorConsumer} to receive the error of the invocation
     *
     * @return the {@link Future<Object>} to handle the return value
     */
    Future<Object> invokeFuture(
            List<Object> address,
            Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            InvocationErrorConsumer asyncInvocationErrorConsumer);

    /**
     * Invokes the method returning a {@link Void} (ie null) for the value.
     *
     * Corresponds to {@link RemoteInvoker#invokeFuture(Invocation, List, InvocationErrorConsumer)}.
     *
     * @param address the address
     * @param invocation the {@link Invocation}
     * @param asyncInvocationResultConsumerList the list of {@link Consumer<InvocationResult>} instances
     * @param asyncInvocationErrorConsumer the {@link InvocationErrorConsumer} to receive the error of the invocation
     *
     * @return null
     */
    Void invokeAsync(
            List<Object> address,
            Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            InvocationErrorConsumer asyncInvocationErrorConsumer);

    /**
     * Invokes the method, blocking until it returns on the remote end.
     *
     * Corresponds to {@link RemoteInvoker#invokeFuture(Invocation, List, InvocationErrorConsumer)}.
     *
     * @param address the address
     * @param invocation the {@link Invocation}
     * @param asyncInvocationResultConsumerList the list of {@link Consumer<InvocationResult>} instances
     * @param asyncInvocationErrorConsumer the {@link InvocationErrorConsumer} to receive the error of the invocation
     *
     * @return the return value of the remote invocation
     */
    Object invokeSync(
            List<Object> address,
            Invocation invocation,
            List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
            InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception;

}
