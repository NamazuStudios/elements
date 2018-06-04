package com.namazustudios.socialengine.rt;

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
     * Performs an operation against the supplied module.  This allows an operation to be performed against a
     * {@link Resource}.  Additionally, this takes care to handle all interation with the {@link ResourceLockService}
     * ensuring that only one thread at a time is accessing the provided {@link Resource}.
     *  @param attributes the module attributes
     * @param module the module name
     * @param resourceConsumer a {@link Consumer<Resource>} to perform the operation.
     */
    void perform(Attributes attributes, String module, Consumer<Resource> resourceConsumer);

}
