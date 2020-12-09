package com.namazustudios.socialengine.rt;

import javax.inject.Named;

/**
 * Represents the connection the backend cluster of services.
 */
public interface Context {

    /**
     * Used with the {@link Named} annotation to designate context types which are local, as in they are not sent
     * via remote invocation.
     */
    String LOCAL = "com.namazustudios.socialengine.rt.context.local";

    /**
     * Used with the {@link Named} annotation to designate context types which are remote, as in they are sent via
     * remote invocation.
     */
    String REMOTE = "com.namazustudios.socialengine.rt.context.remote";

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

    /**
     * Gets the {@link IndexContext} assocaited with this {@link Context}
     *
     * @return the {@link IndexContext}
     */
    IndexContext getIndexContext();

    /**
     * Gets the {@Link HandlerContext}.
     *
     * @return the {@link HandlerContext}
     */
    HandlerContext getHandlerContext();

    /**
     * Gets the {@link TaskContext}.
     *
     * @return the {@link TaskContext}
     */
    TaskContext getTaskContext();

}
