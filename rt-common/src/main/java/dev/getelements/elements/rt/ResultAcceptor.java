package dev.getelements.elements.rt;

import dev.getelements.elements.rt.id.TaskId;

import java.util.function.Consumer;

/**
 * Provides a means to accept results returned by instances of {@link MethodDispatcher}
 */
public interface ResultAcceptor<T> {

    /**
     * Calling calling this method completes the dispatch tho th underlying {@link Resource}'s method and will place the
     * response of the method call into the provided {@link Consumer<T>}.  Calling this should kick off a new task.
     *
     * If this method returns, then it has succeeded in scheduling the task.  It will throw an exception if it doesn't
     * and will not invoke the supplied {@link Consumer<Throwable>}.
     *
     * @param tConsumer the consumer
     * @param throwableTConsumer the {@link Consumer<Throwable>} to receive errors
     * @return the {@link TaskId} of the task associated with servicing the invocation
     */
    TaskId dispatch(Consumer<T> tConsumer, Consumer<Throwable> throwableTConsumer);

}
