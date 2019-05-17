package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.*;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable.RoutingStrategy;
import com.namazustudios.socialengine.rt.remote.AddressedRoutingStrategy;
import com.namazustudios.socialengine.rt.util.SyncWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.Attributes.emptyAttributes;

/**
 * The interface for manipulating {@link Resource}s in the cluster.
 */
@Proxyable
public interface ResourceContext {

    /**
     * Starts this {@link ResourceContext}.
     */
    default void start() {}

    /**
     * Stops this {@link ResourceContext}.
     */
    default void stop() {}

    /**
     * Creates a {@link Resource} at the provided {@link Path}.
     *
     * @param module the module to instantiate
     * @param path the path
     * @param args the arguments to pass to the module instantiation
     *
     * @return the system-assigned {@link ResourceId}
     */
    default ResourceId create(@Serialize final String module,
                              @Serialize final Path path,
                              @Serialize final Object... args) {
        final SyncWait<ResourceId> resourceIdSyncWait = new SyncWait<>(getClass());
        createAsync(resourceIdSyncWait.getResultConsumer(), resourceIdSyncWait.getErrorConsumer(),
                    module, path, args);
        return resourceIdSyncWait.get();
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
     *
     */
    default void createAsync(@ResultHandler final Consumer<ResourceId> success,
                             @ErrorHandler  final Consumer<Throwable> failure,
                             @Serialize final String module,
                             @Serialize final Path path,
                             @Serialize final Object... args) {
        createAttributesAsync(success, failure, module, path, emptyAttributes(), args);
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
    default ResourceId createAttributes(@Serialize final String module,
                                        @Serialize final Path path,
                                        @Serialize final Attributes attributes,
                                        @Serialize final Object... args) {
        final SyncWait<ResourceId> resourceIdSyncWait = new SyncWait<>(getClass());
        createAttributesAsync(resourceIdSyncWait.getResultConsumer(), resourceIdSyncWait.getErrorConsumer(),
                              module, path, attributes, args);
        return resourceIdSyncWait.get();
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
    @RemotelyInvokable(AnyOrAddressedRoutingStrategy.class)
    void createAttributesAsync(@ResultHandler Consumer<ResourceId> success,
                               @ErrorHandler  Consumer<Throwable> failure,
                               @Serialize String module,
                               @Serialize Path path,
                               @Serialize Attributes attributes,
                               @Serialize Object... args);

    /**
     * Synchronous invoke of {@link #invokePathAsync(Consumer, Consumer, Path, String, Object...)}.
     *
     * @param resourceId the resource's id
     * @param method the method name
     * @param args the argument array
     * @return the result of the invocation
     */
    default Object invoke(@Serialize final ResourceId resourceId,
                          @Serialize final String method,
                          @Serialize final Object... args) {
        final SyncWait<Object> resultSyncWait = new SyncWait<>(getClass());
        invokeAsync(resultSyncWait.getResultConsumer(), resultSyncWait.getErrorConsumer(),
                    resourceId, method, args);
        return resultSyncWait.get();
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
    @RemotelyInvokable(AddressedRoutingStrategy.class)
    void invokeAsync(@ResultHandler Consumer<Object> success,
                     @ErrorHandler  Consumer<Throwable> failure,
                     @Serialize ResourceId resourceId,
                     @Serialize String method,
                     @Serialize Object... args);

    /**
     * Synchronous invoke of {@link #invokePathAsync(Consumer, Consumer, Path, String, Object...)}.
     *
     * @param path the path
     * @param method the method name
     * @param args the argument array
     * @return the result of the invocation
     */
    default Object invokePath(@Serialize final Path path,
                              @Serialize final String method,
                              @Serialize final Object... args) {
        final SyncWait<Object> resultSyncWait = new SyncWait<>(getClass());
        invokePathAsync(resultSyncWait.getResultConsumer(), resultSyncWait.getErrorConsumer(),
                        path, method, args);
        return resultSyncWait.get();
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
    @RemotelyInvokable(AddressedRoutingStrategy.class)
    void invokePathAsync(@ResultHandler Consumer<Object> success,
                         @ErrorHandler  Consumer<Throwable> failure,
                         @Serialize Path path, @Serialize String method, @Serialize Object... args);

    /**
     * Destroys the {@link Resource} with the provided {@link ResourceId}.
     *
     * @param resourceId the {@link ResourceId}
     */
    default void destroy(@Serialize final ResourceId resourceId) {
        final SyncWait<Void> resultSyncWait = new SyncWait<>(getClass());
        destroyAsync(resultSyncWait.getResultConsumer(), resultSyncWait.getErrorConsumer(), resourceId);
        resultSyncWait.get();
    }

    /**
     * Destroys the {@link Resource} with the provided {@link ResourceId}.
     *  @param success called if the operation succeeds
     * @param failure called if the operation fails
     * @param resourceId the {@link ResourceId}
     */
    @RemotelyInvokable(AddressedRoutingStrategy.class)
    void destroyAsync(@ResultHandler Consumer<Void> success,
                      @ErrorHandler  Consumer<Throwable> failure,
                      @Serialize     ResourceId resourceId);

    /**
     * Destroys the {@link Resource} using the {@link ResourceId} {@link String}.
     *
     * @param resourceIdString the {@link ResourceId} {@link String}.
     */
    default void destroyAsync(@ResultHandler Consumer<Void> success,
                              @ErrorHandler  Consumer<Throwable> failure,
                              @Serialize     String resourceIdString) {
        destroyAsync(success, failure, new ResourceId(resourceIdString));
    }

    /**
     * Performs the operations of {@link #destroyAllResourcesAsync(Consumer, Consumer)} in a synchronous fashion.
     */
    default void destroyAllResources() {
        final SyncWait<Void> resultSyncWait = new SyncWait<>(getClass());
        destroyAllResourcesAsync(resultSyncWait.getResultConsumer(), resultSyncWait.getErrorConsumer());
        resultSyncWait.get();
    }

    /**
     * Clears all {@link Resource} instances from the system.  This is a call that should be used with extreme care
     * as it can possibly destroy the whole system.  This is primarily used for testing, or restarting or shutting down
     * handler instances which are intended to be short-lived.
     *
     * This may not exist for all implementations of {@link ResourceContext}, and may simply provide an exception
     * indicating so.
     */
    @RemotelyInvokable(AddressedRoutingStrategy.class)
    void destroyAllResourcesAsync(@ResultHandler Consumer<Void> success,
                                  @ErrorHandler  Consumer<Throwable> failure);

}
