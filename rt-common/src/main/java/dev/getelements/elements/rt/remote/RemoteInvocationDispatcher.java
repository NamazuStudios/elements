package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.annotation.Dispatch;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface RemoteInvocationDispatcher {

    /**
     * Starts up this dispatcher.
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
     * Invokes {@link #invokeAsyncV(Route, Invocation, List, InvocationErrorConsumer)}, ignoring the returned
     * {@link AsyncOperation}.
     */
    default Void invokeAsyncV(Route route,
                              Invocation invocation,
                              List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                              InvocationErrorConsumer asyncInvocationErrorConsumer) {
        invokeAsync(route, invocation, asyncInvocationResultConsumerList, asyncInvocationErrorConsumer);
        return null;
    }

    /**
     * Sends the {@link Invocation} to the remote service and waits for the {@link InvocationResult}.  The supplied
     * {@link Consumer< InvocationError >} will relay all encountered errors.
     *
     * Typically this is used with the {@link Dispatch.Type#ASYNCHRONOUS}.
     *
     * This returns the generic Void type to clarify the intention of the method that the underlying {@link Future}
     * is discarded, but since is is intended to be used with reflections code, this allows for the method to be easily
     * adapted as such.
     *
     * @param route the {@link Route} through which to send the {@link Invocation}
     * @param invocation the outgoing {@link Invocation}
     * @param asyncInvocationResultConsumerList a {@link List<Consumer<InvocationResult>>} to capture all async results
     * @param asyncInvocationErrorConsumer a {@link Consumer<InvocationError>} to receive async errors
     * @return a null {@link Void}, for the sake of clarity
     */
    AsyncOperation invokeAsync(Route route,
                               Invocation invocation,
                               List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                               InvocationErrorConsumer asyncInvocationErrorConsumer);

    /**
     * Sends the {@link Invocation} to the remote service and waits for the {@link InvocationResult}.  The supplied
     * {@link Consumer< InvocationError >} will relay all encountered errors.
     *
     * Typically this is used with the {@link Dispatch.Type#FUTURE}
     *
     * @param route the {@link Route} through which to send the {@link Invocation}
     * @param invocation the outgoing {@link Invocation}
     * @param asyncInvocationResultConsumerList a {@link List<Consumer<InvocationResult>>} to capture all async results
     * @param asyncInvocationErrorConsumer a {@link Consumer<InvocationError>} to receive async errors
     * @return a {@link Future<Object>} which returns the result of the remote invocation
     */
    Future<Object> invokeFuture(Route route,
                                Invocation invocation,
                                List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                                InvocationErrorConsumer asyncInvocationErrorConsumer);

    /**
     * Sends the {@link Invocation} to the remote service and waits for the {@link InvocationResult}.  The supplied
     * {@link Consumer< InvocationError >} will relay all encountered errors.
     *
     * Typically this is used with the {@link Dispatch.Type#SYNCHRONOUS}
     *
     * The default implementation of this method simply uses the {@link Future<Object>} returned by the method defined
     * by {@link #invokeFuture(Route, Invocation, List, InvocationErrorConsumer)} and blocks on {@link Future#get()}.  However,
     * the underlying implementation should override this method to implement a more efficient means of blocking, such
     * as actually blocking on the underlying network socket.
     *
     * @param route the {@link Route} through which to send the {@link Invocation}
     * @param invocation the outgoing {@link Invocation}
     * @param asyncInvocationResultConsumerList a {@link List<Consumer<InvocationResult>>} to capture all async results
     * @param asyncInvocationErrorConsumer a {@link Consumer<InvocationError>} to receive async errors
     * @return the result of the remote {@link Invocation}
     */
    Object invokeSync(Route route,
                      Invocation invocation,
                      List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                      InvocationErrorConsumer asyncInvocationErrorConsumer) throws Exception;

}
