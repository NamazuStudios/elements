package com.namazustudios.socialengine.rt;

/**
 * Represents the connection the backend cluster of services.
 */
@Proxyable
public interface Context {

    /**
     * Starts the context.
     */
    void start();

    /**
     * Shuts down this {@link Context} and disconnecting this {@link Context}.  The default implementation simply
     * defers all work to the managed services.
     */
    void shutdown();

    /**
     * Gets the {@link ResourceContext} assocaited with this {@link Context}
     *
     * @return the {@link ResourceContext}
     */
    ResourceContext getResourceContext();

    /**
     * Gets the {@link SchedulerContext} assocaited with this {@link Context}
     *
     * @return the {@link SchedulerContext}
     */
    SchedulerContext getSchedulerContext();

}
