package com.namazustudios.socialengine.rt.remote;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Holds a connection to the remote service and dispatches {@link Invocation}.
 */
public interface RemoteInvoker {

    /**
     * Sends the {@link Invocation} to the remote service and waits for the {@link InvocationResult}.  The supplied
     * {@link Consumer<InvocationError>} will relay all encountered errors.
     *
     * @param invocation the outgoing {@link Invocation}
     * @param errorInvocationResultConsumer a {@link Consumer<InvocationError>} to receive all errors
     * @param invocationResultConsumerList a {@link List<Consumer<InvocationResult>>} to capture all results
     *
     * @return a {@link Future<Object>} which returns the result of the remote invocation
     */
    Future<Object> invoke(Invocation invocation,
                          Consumer<InvocationError> errorInvocationResultConsumer,
                          List<Consumer<InvocationResult>> invocationResultConsumerList);

}
