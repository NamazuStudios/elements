package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.Proxyable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.Attributes.emptyAttributes;
import static com.namazustudios.socialengine.rt.Context._waitAsync;

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
    default ResourceId create(final String module, final Path path, final Object... args) {

        final Logger logger = LoggerFactory.getLogger(getClass());

        final Future<ResourceId> future = createAttributesAsync(
                resourceId  -> logger.info("Created {} -> {}", resourceId, path),
                th -> logger.error("Failed create at {}", path, th),
                module, path, emptyAttributes(), args);

        return _waitAsync(logger, future);

    }

    /**
     * Creates a {@link Resource} passing the default {@link Attributes} obtained from
     * {@link Attributes#emptyAttributes()}.
     *
     * @param success the {@Consumer<ResourceId>} which will be called if the call succeeds
     * @param failure the {@Consumer<Throwable>} which will be called if the call fails, capturing any failure
     * @param module module name to instantiate
     * @param path the {@link Path} for the {@link Resource}
     * @param args the arguments to pass to the {@link Resource} on initialization
     * @return
     */
    default Future<ResourceId> createAsync(Consumer<ResourceId> success, Consumer<Throwable> failure,
                                           String module, Path path, Object... args) {
        return createAttributesAsync(success, failure, module, path, emptyAttributes(), args);
    }

    /**
     * Creates a {@link Resource} at the provided {@link Path}.
     *
     * @param module the module to instantiate
     * @param path the path
     * @param args the arguments to pass to the module instantiation
     *
     * @return the system-assigned {@link ResourceId}
     */
    default ResourceId createAttributes(final String module, final Path path,
                                        final Attributes attributes, final Object... args) {

        final Logger logger = LoggerFactory.getLogger(getClass());

        final Future<ResourceId> future = createAttributesAsync(
            resourceId  -> logger.info("Created {} -> {}", resourceId, path),
            th -> logger.error("Failed create at {}", path, th),
            module, path, attributes, args);

        return _waitAsync(logger, future);

    }

    /**
     * Creates a {@link Resource} asynchronously.  Once created, the {@link Resource} can be provided to the supplied
     * {@link Consumer<Resource>}.
     *
     * @param success the {@Consumer<ResourceId>} which will be called if the call succeeds
     * @param failure the {@Consumer<Throwable>} which will be called if the call fails, capturing any failure
     * @param module module name to instantiate
     * @param path the {@link Path} for the {@link Resource}
     * @param args the arguments to pass to the {@link Resource} on initialization
     */
    Future<ResourceId> createAttributesAsync(Consumer<ResourceId> success, Consumer<Throwable> failure,
                                             String module, Path path, Attributes attributes, Object... args);

    /**
     * Synchronous invoke of {@link #invokePathAsync(Consumer, Consumer, Path, String, Object...)}.
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

        return _waitAsync(logger, future);

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
     * Synchronous invoke of {@link #invokePathAsync(Consumer, Consumer, Path, String, Object...)}.
     *
     * @param path the path
     * @param method the method name
     * @param args the argument array
     * @return the result of the invocation
     */
    default Object invokePath(final Path path, final String method, final Object... args) {

        final Logger logger = LoggerFactory.getLogger(getClass());

        final Future<Object> future = invokePathAsync(
            object  -> logger.info("Invoked {}:{}({})", path.toNormalizedPathString(), method, Arrays.toString(args)),
            throwable -> logger.info("Invvocation failed {}:{}({})", path.toNormalizedPathString(), method, Arrays.toString(args), throwable),
            path, method, args);

        return _waitAsync(logger, future);

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
    Future<Object> invokePathAsync(Consumer<Object> success, Consumer<Throwable> failure,
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

        _waitAsync(logger, future);

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
     * Performs the operations of {@link #destroyAllResourcesAsync(Consumer, Consumer)} in a synchronous fashion.
     */
    default void destroyAllResources() {

        final Logger logger = LoggerFactory.getLogger(getClass());

        final Future<Void> future = destroyAllResourcesAsync(
                v  -> logger.info("Destroyed all resources."),
                th -> logger.error("Failed to destroy all resources", th));

        _waitAsync(logger, future);

    }

    /**
     * Clears all {@link Resource} instances from the system.  This is a call that should be used with extreme care
     * as it can possibly destroy the whole system.  This is primarily used for testing, or restarting or shutting down
     * handler instances which are intended to be short-lived.
     *
     * This may not exist for all implementations of {@link ResourceContext}, and may simply provide an exception
     * indicating so.
     */
    Future<Void> destroyAllResourcesAsync(Consumer<Void> success, Consumer<Throwable> failure);

}
