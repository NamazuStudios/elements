package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.BaseException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * The interface for manipulating {@link Resource}s in the cluster.
 */
@Proxyable
public interface ResourceContext {

    /**
     * Creates a {@link Resource} at the provided {@link Path}.
     *
     * @param path the path
     * @param module the module to instantiate
     * @param args the arguments to pass to the module instantiation
     *
     * @return the system-assigned {@link ResourceId}
     */
    default ResourceId create(Path path, String module, Object ... args) {

        final Logger logger = LoggerFactory.getLogger(getClass());

        final Future<ResourceId> future = createAsync(
                resourceId  -> logger.info("Created {} -> {}", resourceId, path),
                th -> logger.error("Failed create at {}", path, th),
                path, module, args);

        return waitAsync(future);

    }

    /**
     * Creates a {@link Resource} asynchronously.  Once created, the {@link Resource} can be provided to the supplied
     * {@link Consumer<Resource>} in case any operations
     *  @param success the {@Consumer<ResourceId>} which will be called if the call succeeds
     * @param failure the {@Consumer<Throwable>} which will be called if the call fails, capturing any failure
     * @param path the {@link Path} for the {@link Resource}
     * @param module module name to instantiate
     * @param args the arguments to pass to the {@link Resource} on initialization
     */
    Future<ResourceId> createAsync(Consumer<ResourceId> success, Consumer<Throwable> failure,
                                   Path path, String module, Object ... args);

    /**
     * Destroys the {@link Resource} with the provided {@link ResourceId}.
     *
     * @param resourceId the {@link ResourceId}
     */
    default void destroy(final ResourceId resourceId) {

        final Logger logger = LoggerFactory.getLogger(getClass());

        final Future<Void> future = destroyAsync(resourceId,
            v  -> logger.info("Destroyed {}", resourceId),
            th -> logger.error("Failed to destroy {}", resourceId, th));

        waitAsync(future);

    }

    /**
     * Destroys the {@link Resource} with the provided {@link ResourceId}.
     *
     * @param resourceId the {@link ResourceId}
     * @param success called if the operation succeeds
     * @param failure called if the operation fails
     */
    Future<Void> destroyAsync(ResourceId resourceId, Consumer<Void> success, Consumer<Throwable> failure);

    /**
     * Destroys the {@link Resource} using the {@link ResourceId} {@link String}.
     *
     * @param resourceIdString the {@link ResourceId} {@link String}.
     */
    default Future<Void> destroyAsync(String resourceIdString, Consumer<Void> success, Consumer<Throwable> failure) {
        return destroyAsync(new ResourceId(resourceIdString), success, failure);
    }

    /**
     * Used to assist implementations with handling {@link Future} types.
     *
     * @param tFuture the {@link Future}
     * @param <T> the type of the {@link Future}
     * @return the result of {@link Future#get()}
     */
    default <T> T waitAsync(Future<T> tFuture) {

        final Logger logger = LoggerFactory.getLogger(getClass());

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
