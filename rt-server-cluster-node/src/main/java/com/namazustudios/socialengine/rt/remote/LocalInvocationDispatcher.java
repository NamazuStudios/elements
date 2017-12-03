package com.namazustudios.socialengine.rt.remote;

import java.util.function.Consumer;

/**
 * Implements the strategy by which the {@link Invocation} is dispatched to the local {@link Object} in memory.
 */
public interface LocalInvocationDispatcher {

    /**
     * Performs the dispatch to the supplied object.  Catching and collecting any errors appropriately, and forwarding
     * the result to the supplied {@link Consumer<InvocationResult>}.
     *
     * @param target the target {@link Object}
     * @param invocation the {@link Invocation} to send
     * @param invocationResultConsumer the {@link Consumer<InvocationResult>} which will receive the result
     */
    void dispatch(Object target, Invocation invocation, Consumer<InvocationResult> invocationResultConsumer);

}
