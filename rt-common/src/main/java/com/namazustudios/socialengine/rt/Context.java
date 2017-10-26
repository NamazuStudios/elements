package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.BaseException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    /**
     * Gets the {@link IndexContext} assocaited with this {@link Context}
     *
     * @return the {@link IndexContext}
     */
    IndexContext getIndexContext();

    /**
     * Used to assist implementations with handling {@link Future} types.  Because this is provided at the interface
     * level, it must be public.  However, this should only be used within the implementation of {@link Context} and
     * its related types.
     *
     *
     * @param tFuture the {@link Future}
     * @param <T> the type of the {@link Future}
     * @return the result of {@link Future#get()}
     */
    static <T> T _waitAsync(final Logger logger, Future<T> tFuture) {
        try {
            return tFuture.get();
        } catch (InterruptedException e) {
            logger.error("Interrupted.", e);
            throw new InternalException(e);
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof BaseException) {
                throw (BaseException) e.getCause();
            } else {
                throw new InternalException(e.getCause());
            }
        }
    }

}
