package com.namazustudios.socialengine.rt.remote;

import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Holds a connection to the remote service and dispatches {@link Invocation}.
 */
public interface RemoteInvoker {

    /**
     * Sends the {@link Invocation} to the remote service and waits for the {@link InvocationResult}.
     *
     * @param invocation the outoing {@link Invocation}
     * @param invocationResultConsumerConsumer receives the {@link InvocationResult} when the remote method returns
     *
     * @return a {@link Future<Object>} which returns the result of the remote invocation
     */
    Future<Object> invoke(Invocation invocation, Consumer<InvocationResult> invocationResultConsumerConsumer);

}
