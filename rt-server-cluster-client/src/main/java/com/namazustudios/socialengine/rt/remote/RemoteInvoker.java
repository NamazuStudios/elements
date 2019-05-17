package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.annotation.Dispatch.*;

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
     *
     * @deprecated This maps directly to {@link #invokeAsync(Invocation, List, InvocationErrorConsumer)}, but was
     * renamed for clarity on the behavior.  This is not very useful because it can force more threads than are
     * necessary to create a remote invocation.
     *
     * @param invocation the outgoing {@link Invocation}
     * @param asyncInvocationResultConsumerList a {@link List<Consumer<InvocationResult>>} to capture all async results
     * @param asyncInvocationErrorConsumer a {@link Consumer<InvocationError>} to receive async errors
     * @return a {@link Future<Object>} which returns the result of the remote invocation
     */
    @Deprecated
    default Future<Object> invoke(Invocation invocation,
                                  List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                  InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return invokeAsync(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
    }

    /**
     * Sends the {@link Invocation} to the remote service and waits for the {@link InvocationResult}.  The supplied
     * {@link Consumer< InvocationError >} will relay all encountered errors.
     *
     * Typically this is used with the {@link Type#CONSUMER}.
     *
     * This returns the generic Void type to clarify the intention of the method that the underlying {@link Future}
     * is discarded, but since is is intended to be used with reflections code, this allows for the method to be easily
     * adapted as such.
     *
     * @param invocation the outgoing {@link Invocation}
     * @param asyncInvocationResultConsumerList a {@link List<Consumer<InvocationResult>>} to capture all async results
     * @param asyncInvocationErrorConsumer a {@link Consumer<InvocationError>} to receive async errors
     * @return a null {@link Void}, for the sake of clarity
     */
    default Void invokeAsyncV(Invocation invocation,
                                List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                InvocationErrorConsumer asyncInvocationErrorConsumer) {
        invokeAsync(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
        return null;
    }

    /**
     * Sends the {@link Invocation} to the remote service and waits for the {@link InvocationResult}.  The supplied
     * {@link Consumer< InvocationError >} will relay all encountered errors.
     *
     * Typically this is used with the {@link Type#FUTURE}
     *
     * @param invocation the outgoing {@link Invocation}
     * @param asyncInvocationResultConsumerList a {@link List<Consumer<InvocationResult>>} to capture all async results
     * @param asyncInvocationErrorConsumer a {@link Consumer<InvocationError>} to receive async errors
     * @return a {@link Future<Object>} which returns the result of the remote invocation
     */
    Future<Object> invokeAsync(Invocation invocation,
                               List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                               InvocationErrorConsumer asyncInvocationErrorConsumer);

    /**
     * Sends the {@link Invocation} to the remote service and waits for the {@link InvocationResult}.  The supplied
     * {@link Consumer< InvocationError >} will relay all encountered errors.
     *
     * Typically this is used with the {@link Type#SYNCHRONOUS}
     *
     * The default implementation of this method simply uses the {@link Future<Object>} returned by the method defined
     * by {@link #invokeAsync(Invocation, List, InvocationErrorConsumer)} and blocks on {@link Future#get()}.  However,
     * the underlying implementation should override this method to implement a more efficient means of blocking, such
     * as actually blocking on the underlying network socket.
     *
     * @param invocation the outgoing {@link Invocation}
     * @param asyncInvocationResultConsumerList a {@link List<Consumer<InvocationResult>>} to capture all async results
     * @param asyncInvocationErrorConsumer a {@link Consumer<InvocationError>} to receive async errors
     * @return the result of the remote {@link Invocation}
     */
    default Object invokeSync(Invocation invocation,
                              List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                              InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception {
        try {
            final Future<Object> objectFuture;
            objectFuture = invokeAsync(invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
            return objectFuture.get();
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException)ex.getCause();
            } else {
                throw new InternalException(ex);
            }
        } catch (InterruptedException ex) {
            throw new InternalException("Interrupted waiting.", ex);
        }
    }

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
        void accept(InvocationError invocationError);

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
