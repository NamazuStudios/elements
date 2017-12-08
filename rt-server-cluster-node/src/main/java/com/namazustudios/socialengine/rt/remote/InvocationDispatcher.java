package com.namazustudios.socialengine.rt.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Dispatches {@link Invocation} instances to the objects in the container, collects the result, and relays any errors.
 */
public interface InvocationDispatcher {

    /**
     * Performs the actual dispatch by unpacking the {@link Invocation} and collecting the results into the provided
     * {@link Consumer<InvocationResult>}.  The supplied {@link Consumer<InvocationResult>}.
     *  @param invocation
     * @param invocationErrorConsumer
     * @param returnInvocationResultConsumer
     * @param additionalInvocationResultConsumerList
     */
    void dispatch(Invocation invocation,
                  Consumer<InvocationError> invocationErrorConsumer,
                  Consumer<InvocationResult> returnInvocationResultConsumer,
                  List<Consumer<InvocationResult>> additionalInvocationResultConsumerList);

}
