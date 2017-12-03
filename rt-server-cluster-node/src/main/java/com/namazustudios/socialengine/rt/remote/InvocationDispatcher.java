package com.namazustudios.socialengine.rt.remote;

import java.util.function.Consumer;

/**
 * Dispatches {@link Invocation} instances to the objects in the container, collects the result, and relays any errors.
 */
public interface InvocationDispatcher {

    /**
     * Performs the actual dispatch by unpacking the {@link Invocation} and collecting the results into the provided
     * {@link Consumer<InvocationResult>}.  The supplied {@link Consumer<InvocationResult>}.
     *
     * @param invocation
     * @param invocationResultConsumer
     */
    void dispatch(Invocation invocation, Consumer<InvocationResult> invocationResultConsumer);

}
