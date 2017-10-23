package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.BaseException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
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
     * @param module the module to instantiate
     * @param path the path
     * @param args the arguments to pass to the module instantiation
     *
     * @return the system-assigned {@link ResourceId}
     */
    default ResourceId create(String module, Path path, Object... args) {

        final Logger logger = LoggerFactory.getLogger(getClass());

        final Future<ResourceId> future = createAsync(
                resourceId  -> logger.info("Created {} -> {}", resourceId, path),
                th -> logger.error("Failed create at {}", path, th),
                module, path, args);

        return _waitAsync(future);

    }

    /**
     * Creates a {@link Resource} asynchronously.  Once created, the {@link Resource} can be provided to the supplied
     * {@link Consumer<Resource>} in case any operations
     * @param success the {@Consumer<ResourceId>} which will be called if the call succeeds
     * @param failure the {@Consumer<Throwable>} which will be called if the call fails, capturing any failure
     * @param module module name to instantiate
     * @param path the {@link Path} for the {@link Resource}
     * @param args the arguments to pass to the {@link Resource} on initialization
     */
    Future<ResourceId> createAsync(Consumer<ResourceId> success, Consumer<Throwable> failure,
                                   String module, Path path, Object... args);

    /**
     * Synchronous invoke of {@link #invokeAsync(Consumer, Consumer, Path, String, Object...)}.
     *
     * @param resourceId the resource's id
     * @param method the method name
     * @param args the argument array
     * @return the result of the invocation
     */
    default Object invoke(final ResourceId resourceId, final String method, final Object... args) {

        final Logger logger = LoggerFactory.getLogger(getClass());

        final Future<Object> future = invokeAsync(
                object  -> logger.info("Invoked {}:{}({})", resourceId.toString(), method, Arrays.toString(args)),
                throwable -> logger.info("Invvocation failed {}:{}({})", resourceId.toString(), method, Arrays.toString(args), throwable),
                resourceId, method, args);

        return _waitAsync(future);

    }

    /**
     * Invokes the method on the {@link ResourceId}.
     *
     * @param success called when the operation succeeds
     * @param failure called when the operation fails
     * @param resourceId the resource's id
     * @param method the method
     * @param args the args
     * @return
     */
    Future<Object> invokeAsync(Consumer<Object> success, Consumer<Throwable> failure,
                               ResourceId resourceId, String method, Object... args);

    /**
     * Synchronous invoke of {@link #invokeAsync(Consumer, Consumer, Path, String, Object...)}.
     *
     * @param path the path
     * @param method the method name
     * @param args the argument array
     * @return the result of the invocation
     */
    default Object invoke(final Path path, final String method, final Object... args) {

        final Logger logger = LoggerFactory.getLogger(getClass());

        final Future<Object> future = invokeAsync(
            object  -> logger.info("Invoked {}:{}({})", path.toNormalizedPathString(), method, Arrays.toString(args)),
            throwable -> logger.info("Invvocation failed {}:{}({})", path.toNormalizedPathString(), method, Arrays.toString(args), throwable),
            path, method, args);

        return _waitAsync(future);

    }

    /**
     * Invokes the method on the {@link Resource} {@link Path}.
     *
     * @param success called when the operation succeeds
     * @param failure called when the operation fails
     * @param path the Path
     * @param method the method
     * @param args the args
     * @return
     */
    Future<Object> invokeAsync(Consumer<Object> success, Consumer<Throwable> failure,
                               Path path, String method, Object... args);

    /**
     * Destroys the {@link Resource} with the provided {@link ResourceId}.
     *
     * @param resourceId the {@link ResourceId}
     */
    default void destroy(final ResourceId resourceId) {

        final Logger logger = LoggerFactory.getLogger(getClass());

        final Future<Void> future = destroyAsync(
            v  -> logger.info("Destroyed {}", resourceId),
            th -> logger.error("Failed to destroy {}", resourceId, th), resourceId);

        _waitAsync(future);

    }

    /**
     * Destroys the {@link Resource} with the provided {@link ResourceId}.
     *  @param success called if the operation succeeds
     * @param failure called if the operation fails
     * @param resourceId the {@link ResourceId}
     */
    Future<Void> destroyAsync(Consumer<Void> success, Consumer<Throwable> failure, ResourceId resourceId);

    /**
     * Destroys the {@link Resource} using the {@link ResourceId} {@link String}.
     *
     * @param resourceIdString the {@link ResourceId} {@link String}.
     */
    default Future<Void> destroyAsync(Consumer<Void> success, Consumer<Throwable> failure, String resourceIdString) {
        return destroyAsync(success, failure, new ResourceId(resourceIdString));
    }

    /**
     * Used to assist implementations with handling {@link Future} types.  Because this is provided at the interface
     * level, it must be public.  However, this should only be used within the implementation of {@link ResourceContext}
     * implementations.
     *
     * @param tFuture the {@link Future}
     * @param <T> the type of the {@link Future}
     * @return the result of {@link Future#get()}
     */
    default <T> T _waitAsync(Future<T> tFuture) {

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
