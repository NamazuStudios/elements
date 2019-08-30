package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.Proxyable;
import com.namazustudios.socialengine.rt.exception.BaseException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Represents the connection the backend cluster of services.
 */
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
