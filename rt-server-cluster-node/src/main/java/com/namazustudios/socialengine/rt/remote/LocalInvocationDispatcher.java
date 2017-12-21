package com.namazustudios.socialengine.rt.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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

    /**
     * If the {@link Method} called from the {@link Invocation} returns a {@link Future} instance, this may defined
     * a strategy for handling the result.  This must result in an eventually invocation of either the supplied
     * {@link Consumer<InvocationError>} or the {@link Consumer<InvocationResult>}
     */
    @FunctionalInterface
    interface ReturnValueStrategy {

        void process(Object object,
                     Consumer<InvocationError> invocationErrorConsumer,
                     Consumer<InvocationResult> returnInvocationResultConsumer);

    }

    /**
     * Constructs a {@link ReturnValueStrategy <Object>} which ignores the return value.
     *
     * @return the {@link ReturnValueStrategy} which passes the result through.
     */
    static ReturnValueStrategy ignoreReturnValueStrategy() {

        final Logger logger = LoggerFactory.getLogger(InvocationDispatcher.class);

        return ((object, invocationErrorConsumer, returnInvocationResultConsumer) -> {
            logger.info("Return value ignored.");

            final InvocationResult invocationResult = new InvocationResult();
            returnInvocationResultConsumer.accept(invocationResult);

        });

    }

    /**
     * Constructs a {@link ReturnValueStrategy <Object>} which simply hands the result of the {@link Invocation}
     * directly to a new instance of {@link InvocationResult} and passes it into to the supplied
     * {@link Consumer<InvocationResult>}
     *
     * @return the {@link ReturnValueStrategy} which passes the result through.
     */
    static ReturnValueStrategy simpleReturnValueStrategy() {
        return ((result, invocationErrorConsumer, returnInvocationResultConsumer) -> {
            final InvocationResult invocationResult = new InvocationResult();
            invocationResult.setResult(result);
            returnInvocationResultConsumer.accept(invocationResult);
        });
    }

    /**
     * Returns an instance of {@link ReturnValueStrategy} which simply blocks until the {@link Future} either returns or
     * throws an instance of {@link Throwable}.
     *
     * @return the {@link ReturnValueStrategy} which blocks until the future finishes
     */
    static ReturnValueStrategy blockingFutureStrategy() {

        final Logger logger = LoggerFactory.getLogger(InvocationDispatcher.class);

        return (future, invocationErrorConsumer, returnInvocationResultConsumer) -> {
            try {
                final Object result = ((Future<?>)future).get();
                final InvocationResult invocationResult = new InvocationResult();
                invocationResult.setResult(result);
                returnInvocationResultConsumer.accept(invocationResult);
            } catch (ExecutionException ex) {
                final InvocationError invocationError = new InvocationError();
                invocationError.setThrowable(ex.getCause());
            } catch (InterruptedException e) {
                logger.info("Interrupted while waiting for result.", e);
            }
        };

    }

}
