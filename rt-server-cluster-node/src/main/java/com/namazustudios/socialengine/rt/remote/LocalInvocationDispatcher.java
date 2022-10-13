package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.ResultHandlerStrategy;

import java.util.List;
import java.util.function.Consumer;

/**
 * Dispatches {@link Invocation} instances to the objects in the container, collects the result, and relays any errors.
 */
public interface LocalInvocationDispatcher {

    /**
     * Performs the actual dispatch by unpacking the {@link Invocation} and collecting the results into the provided
     * {@link Consumer<InvocationResult>}.  The supplied {@link Consumer<InvocationResult>}.
     * @param invocation
     * @param syncInvocationErrorConsumer
     * @param additionalInvocationResultConsumerList
     * @param asyncInvocationErrorConsumer
     */
    void dispatch(Invocation invocation,
                  Consumer<InvocationResult> syncInvocationResultConsumer,
                  Consumer<InvocationError> syncInvocationErrorConsumer,
                  List<Consumer<InvocationResult>> additionalInvocationResultConsumerList,
                  Consumer<InvocationError> asyncInvocationErrorConsumer);

    /**
     * Dispatches using the {@link Invocation} and {@link ResultHandlerStrategy}.
     *
     * @param invocation the {@link Invocation}
     * @param resultHandlerStrategy the {@link ResultHandlerStrategy}
     */
    default void dispatch(final Invocation invocation, final ResultHandlerStrategy resultHandlerStrategy) {
        dispatch(
            invocation,
            resultHandlerStrategy.getSyncResultConsumer(),
            resultHandlerStrategy.getAsyncInvocationErrorConsumer(),
            resultHandlerStrategy.getAsyncInvocationResultConsumers(),
            resultHandlerStrategy.getAsyncInvocationErrorConsumer()
        );
    }

}
