package com.namazustudios.socialengine.rt.remote;

import java.util.List;
import java.util.function.Consumer;

/**
 * Implements the strategy by which the {@link Invocation} is dispatched to the local {@link Object} in memory.
 */
public interface LocalInvocationDispatcher {

    /**
     * Performs the dispatch to the supplied object.  Catching and collecting any errors appropriately, and forwarding
     * the result to the supplied {@link Consumer<InvocationResult>}.  This should not throw an exception, ever.
     *
     * @param target
     * @param invocation
     * @param invocationErrorConsumer
     * @param returnInvocationResultConsumer
     * @param invocationResultConsumerList
     */
    void dispatch(Object target,
                  Invocation invocation,
                  Consumer<InvocationError> invocationErrorConsumer,
                  Consumer<InvocationResult> returnInvocationResultConsumer,
                  List<Consumer<InvocationResult>> invocationResultConsumerList);


}
