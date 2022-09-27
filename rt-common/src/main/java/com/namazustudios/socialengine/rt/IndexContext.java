package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.*;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.routing.ListAggregateRoutingStrategy;
import com.namazustudios.socialengine.rt.routing.SameNodeIdRoutingStrategy;
import com.namazustudios.socialengine.rt.util.SyncWait;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.ELEMENTS_RT_PROTOCOL;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.WORKER_SCOPE;

/**
 * Used to index various {@link Resource} instances by {@link Path}.
 */
@Proxyable
@RemoteService(scopes = @RemoteScope(scope = WORKER_SCOPE, protocol = ELEMENTS_RT_PROTOCOL))
public interface IndexContext {

    /**
     * Starts this {@link IndexContext}.
     */
    default void start() {}

    /**
     * Stops this {@link IndexContext}.
     */
    default void stop() {}

    /**
     * Performs the operations of {@link #listAsync(Path, Consumer, Consumer)} synchronously.
     *
     * @param path the {@link Path} to match
     * @return a {@link List<Listing>} representing all matched {@link Path}s
     */
    default List<Listing> list(@Serialize final Path path) {
        final SyncWait<List<Listing>> streamSyncWait = new SyncWait<>(getClass());
        listAsync(path, v -> streamSyncWait.getResultConsumer(), streamSyncWait.getErrorConsumer());
        return streamSyncWait.get();
    }

    /**
     * Fetches a {@link Listing} of all {@link Path}s and assocaited {@link ResourceId} instances which match the
     * provided {@link Path}.  Unlike other methods for linking and unlinking, the provided {@link Path} may be a
     * wildcard as determined by {@link Path#isWildcard()}.
     *
     * The supplied {@link List<Listing>>} should represent a complete buffering of all {@link Listing} instances
     * matching the {@link Path}.
     *
     * @param path the {@link Path} to match
     * @param success a {@link Consumer<Listing>} which receives an instance of {@link Listing}
     * @param failure a {@link Consumer<Throwable>} which receives an exception indicating a failure reason.
     * @return a {@link Future<List<Listing>>} which can be used to obtain the result
     */
    @RemotelyInvokable(routing = @Routing(ListAggregateRoutingStrategy.class))
    void listAsync(@Serialize Path path,
                   @ResultHandler Consumer<List<Listing>> success,
                   @ErrorHandler  Consumer<Throwable> failure);

    /**
     * Performs the operations of {@link #linkAsync(ResourceId, Path, Consumer, Consumer)} synchronously.
     *
     * @param resourceId the source {@link ResourceId} to link
     * @param destination the {@link Path} of the destination to link
     *
     */
    default void link(@Serialize final ResourceId resourceId,
                      @Serialize final Path destination) {
        final SyncWait<Void> linkSyncWait = new SyncWait<>(getClass());
        linkAsync(resourceId, destination, linkSyncWait.getResultConsumer(), linkSyncWait.getErrorConsumer());
        linkSyncWait.get();
    }

    /**
     * Links a single {@link ResourceId} to a {@link Path} destination.
     *
     * @param resourceId the source {@link ResourceId} to link
     * @param destination the {@link Path} of the destination to link
     * @param success a {@link Consumer<Void>} which will be called on successful completion
     * @param failure @ {@link Consumer<Throwable> which will be called on a failure
     * @return a {@link Future} which can be used to obtain the result of the operation
     *
     *
     */
    @RemotelyInvokable(routing = @Routing(SameNodeIdRoutingStrategy.class))
    void linkAsync(@ProvidesAddress @Serialize ResourceId resourceId,
                   @ProvidesAddress @Serialize Path destination,
                   @ResultHandler Consumer<Void> success,
                   @ErrorHandler  Consumer<Throwable> failure);

    /**
     * Performs the operations {@link #linkPathAsync(Path, Path, Consumer, Consumer)} synchronously.
     *
     * @param source the source {@link Path} to link
     * @param destination the {@link Path} of the destination to link
     */
    default void linkPath(@Serialize Path source, @Serialize Path destination) {
        final SyncWait<Void> linkSyncWait = new SyncWait<>(getClass());
        linkPathAsync(source, destination, linkSyncWait.getResultConsumer(), linkSyncWait.getErrorConsumer());
        linkSyncWait.get();
    }

    /**
     * Links a single {@link Path} to a {@link Path} destination.
     *
     * @param source the source {@link Path} to link
     * @param destination the {@link Path} of the destination to link
     * @param success a {@link Consumer<Void>} which will be called on successful completion
     * @param failure @ {@link Consumer<Throwable> which will be called on a failure
     * @return a {@link Future} which can be used to obtain the result of the operation
     */
    @RemotelyInvokable(routing = @Routing(ListAggregateRoutingStrategy.class))
    void linkPathAsync(@ProvidesAddress @Serialize Path source,
                       @ProvidesAddress @Serialize Path destination,
                       @ResultHandler Consumer<Void> success,
                       @ErrorHandler  Consumer<Throwable> failure);

    /**
     * Performs the operations of {@link #unlinkAsync(Path, Consumer, Consumer)} synchronously.
     *
     * @param path the path to unlink
     * @return an {@link Unlink} representing the result of the operation
     */
    default Unlink unlink(@Serialize final Path path) {
        final SyncWait<Unlink> unlinkSyncWait = new SyncWait<>(getClass());
        unlinkAsync(path, ul -> unlinkSyncWait.getResultConsumer(), unlinkSyncWait.getErrorConsumer());
        return unlinkSyncWait.get();
    }

    /**
     * Unlinks the provided {@link Path} and if this is the last {@link Path} reference to a {@link ResourceId}, then
     * the cluster will remove and destroy the associated {@link Resource}. Further details on the operation can be
     * obtained through the {@link Unlink} interface.
     *
     * If the result is a complete removal, then this will have the same end result as
     * {@link ResourceContext#destroy(ResourceId)} or
     * {@link ResourceContext#destroyAsync(Consumer, Consumer, ResourceId)}.
     *
     * @param path the path to unlink
     * @param success a {@link Consumer<Unlink>} to receive the successful operation
     * @param failure a {@link Consumer<Throwable>} to receive an exception if one was generated
     * @return a {@link Future} which can be used to obtain the result of the operation
     */
    @RemotelyInvokable(routing = @Routing(SameNodeIdRoutingStrategy.class))
    void unlinkAsync(@ProvidesAddress @Serialize Path path,
                     @ResultHandler Consumer<Unlink> success,
                     @ErrorHandler  Consumer<Throwable> failure);

    /**
     * The result of the {@link #unlinkAsync(Path, Consumer, Consumer)} and {@link #unlink(Path)} call.
     */
    interface Unlink {

        /**
         * Returns the {@link ResourceId} that was affected by the unlinking operation.
         *
         * @return the {@link ResourceId}
         */
        ResourceId getResourceId();

        /**
         * Returns true if the {@link ResourceId} was destroyed as part of this unlink operation.
         *
         * @return true if destroyed, false otherwise
         */
        boolean isDestroyed();

    }

    /**
     * Represents an association between a {@link Path} and the {@link ResourceId} to which it points.
     */
    interface Listing {

        /**
         * The {@link Path} of the listing.
         *
         * @return the {@link Path}
             */
        Path getPath();

        /**
         * The {}
         * @return
         */
        ResourceId getResourceId();

    }

}
