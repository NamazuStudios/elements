package com.namazustudios.socialengine.rt.remote;

import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Holds a connection to the remote service and dispatches {@link Invocation}.
 */
public interface RemoteInvoker {

    /**
     * Starts up thin is complete.
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
     * @param asyncInvocationResultConsumerList a {@link List<Consumer<InvocationResult>>} to capture all async results
     * @param asyncInvocationErrorConsumer a {@link Consumer<InvocationError>} to receive async errors
     * @return a {@link Future<Object>} which returns the result of the remote invocation
     */
    Future<Object> invoke(Invocation invocation,
                          List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                          InvocationErrorConsumer asyncInvocationErrorConsumer);

    /**
     * Behaves similar to {@link Consumer<Throwable>} except that it may allow for re-throwing of the underlying
     * {@link Throwable}.
     */
    @FunctionalInterface
    interface InvocationErrorConsumer {

        /**
         * Accepts the {@link Throwable} and processes it.  If necessary it can re-throw it, or wrap it in another type
         * and throw that.
         * @param invocationError the {@link Throwable} instance
         * @throws Throwable if the supplied
         */
        void accept(InvocationError invocationError) throws Exception;

        /**
         * Invokes {@link #accept(InvocationError)}, catching any {@link Throwable} instances and logging them to the
         * supplied instance of {@link Logger}.
         *
         * @param logger the logger to accep the {@link Throwable}
         */
        default void acceptAndLogError(final Logger logger, final InvocationError invocationError) {
            try {
                accept(invocationError);
            } catch (Exception ex) {
                logger.error("Caught throwable handling error.", ex);
            }
        }

    }

}
