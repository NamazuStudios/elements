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
public interface LocalInvocationProcessor {

    /**
     * Performs the dispatch to the supplied object.  Catching and collecting any errors appropriately, and forwarding
     * the result to the supplied {@link Consumer<InvocationResult>}.  This should not throw an exception, ever.
     * @param target
     * @param invocation
     * @param asyncInvocationResultConsumerList
     * @param asyncInvocationErrorConsumer
     */
    void processInvocation(
        Object target, Invocation invocation,
        Consumer<InvocationResult> syncInvocationResultConsumer, Consumer<InvocationError> syncInvocationErrorConsumer,
        List<Consumer<InvocationResult>> asyncInvocationResultConsumerList, Consumer<InvocationError> asyncInvocationErrorConsumer);

    /**
     * If the {@link Method} called from the {@link Invocation} returns a {@link Future} instance, this may defined
     * a strategy for handling the result.  This must result in an eventually invocation of either the supplied
     * {@link Consumer<InvocationError>} or the {@link Consumer<InvocationResult>}
     */
    @FunctionalInterface
    interface ReturnValueStrategy {

        void process(Object object,
                     Consumer<InvocationError> syncInvocationErrorConsumer,
                     Consumer<InvocationResult> syncInvocationResultConsumer);

    }

    /**
     * Constructs a {@link ReturnValueStrategy <Object>} which ignores the return value and immediately returns an
     * {@link InvocationResult} with a null result.
     *
     * @return the {@link ReturnValueStrategy} which passes the result through.
     */
    static ReturnValueStrategy ignoreReturnValueStrategy() {
        return ((object, syncInvocationErrorConsumer, syncInvocationResultConsumer) -> {
            final InvocationResult invocationResult = new InvocationResult();
            syncInvocationResultConsumer.accept(invocationResult);
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
        return ((result, syncInvocationErrorConsumer, syncInvocationResultConsumer) -> {
            final InvocationResult invocationResult = new InvocationResult();
            invocationResult.setResult(result);
            syncInvocationResultConsumer.accept(invocationResult);
        });
    }

    /**
     * Returns an instance of {@link ReturnValueStrategy} which simply blocks until the {@link Future} either returns or
     * throws an instance of {@link Throwable}.
     *
     * @return the {@link ReturnValueStrategy} which blocks until the future finishes
     */
    static ReturnValueStrategy blockingFutureStrategy() {

        final Logger logger = LoggerFactory.getLogger(LocalInvocationDispatcher.class);

        return (future, syncInvocationErrorConsumer, syncInvocationResultConsumer) -> {
            try {
                final Object result = ((Future<?>)future).get();
                final InvocationResult invocationResult = new InvocationResult();
                invocationResult.setResult(result);
                syncInvocationResultConsumer.accept(invocationResult);
            } catch (ExecutionException ex) {
                final InvocationError invocationError = new InvocationError();
                invocationError.setThrowable(ex.getCause());
                syncInvocationErrorConsumer.accept(invocationError);
            } catch (InterruptedException e) {
                logger.info("Interrupted while waiting for result.", e);
            }
        };

    }

}
