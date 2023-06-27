package dev.getelements.elements.rt;

import dev.getelements.elements.rt.remote.InvocationError;
import dev.getelements.elements.rt.remote.InvocationResult;

import java.util.List;
import java.util.function.Consumer;

/**
 * This defines the strategy of how JSON-RPC handles the response.
 */
public interface ResultHandlerStrategy {

    /**
     * Returns the sync result consumer.
     * @return the invocation result consumer.
     */
    Consumer<InvocationResult> getSyncResultConsumer();

    /**
     * Returns the sync error consumer.
     *
     * @return the sync error consumer.
     */
    Consumer<InvocationError> getSyncErrorConsumer();

    /**
     * Returns the async invocation result consumers.
     *
     * @return the result consumers.
     */
    List<Consumer<InvocationResult>> getAsyncInvocationResultConsumers();

    /**
     * Gets the async invocation error consumers.
     *
     * @return the async error invocation consumers.
     */
    Consumer<InvocationError> getAsyncInvocationErrorConsumer();

    /**
     * Handles the final error result.
     *
     * @return a {@link Subscription}
     */
    Subscription onError(Consumer<Throwable> finalResult);

    /**
     * Handles the final result.
     *
     * @return a {@link Subscription}
     */
    Subscription onFinalResult(Consumer<Object> finalResult);

}
