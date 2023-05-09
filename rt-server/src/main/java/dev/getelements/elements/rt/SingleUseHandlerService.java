package dev.getelements.elements.rt;

import dev.getelements.elements.rt.id.TaskId;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Used to implement handler types.  Handlers are {@link Resource} instances that are meant to handle one-time requests
 * that are invoked by other processes.  This may employ caching to improve performance and avoi disk IO.
 */
public interface SingleUseHandlerService {

    /**
     * Starts this {@link SingleUseHandlerService}
     */
    void start();

    /**
     * Stops this {@link SingleUseHandlerService}
     */
    void stop();

    /**
     * Performs an operation against this {@link SingleUseHandlerService}, reporting either success or failure to the
     * code.
     */
    TaskId perform(Consumer<Object> success, Consumer<Throwable> failure,
                   long timeoutDelay, TimeUnit timeoutUnit,
                   String module, Attributes attributes,
                   String method, Object... args);

}
