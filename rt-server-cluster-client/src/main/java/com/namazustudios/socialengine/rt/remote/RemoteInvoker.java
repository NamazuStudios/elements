package com.namazustudios.socialengine.rt.remote;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Holds a connection to the remote service and dispatches {@link Invocation}.
 */
public interface RemoteInvoker {

    /**
     * Starts up this {@link RemoteInvoker}.  This may connect to the remote endpoint if necessary and may block until
     * the connection is complete.
     *
     * The default implementation of this method does nothing in case no setup is necessary.
     */
    default void start() {}

    /**
     * Stops this {@link RemoteInvoker}.  This method must gracefully shut down all connections and stop any worker
     * threads.
     *
     * THe default implementation of this method does nothing in case no shutdown is necessary.
     *
     */
    default void stop() {}

    /**
     * Sends the {@link Invocation} to the remote service and waits for the {@link InvocationResult}.  The supplied
     * {@link Consumer< InvocationError >} will relay all encountered errors.
     *
     * @param invocation the outgoing {@link Invocation}
     * @param invocationErrorConsumer a {@link Consumer<InvocationError>} to receive all errors
     * @param invocationResultConsumerList a {@link List<Consumer<InvocationResult>>} to capture all results
     *
     * @return a {@link Future<Object>} which returns the result of the remote invocation
     */
    Future<Object> invoke(Invocation invocation,
                          Consumer<InvocationError> invocationErrorConsumer,
                          List<Consumer<InvocationResult>> invocationResultConsumerList);

}
