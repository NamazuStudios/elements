package dev.getelements.elements.rt;


import dev.getelements.elements.rt.exception.DuplicateException;
import dev.getelements.elements.rt.exception.ResourceNotFoundException;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.sdk.cluster.path.Path;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static dev.getelements.elements.sdk.cluster.id.ResourceId.resourceIdFromString;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * This is the service responsible for maintaining a set of {@link Resource} instances.  This contains code to handle a
 * the path hierarchy for the resources housed in the service.
 *
 * Resources can be added, moved, or deleted as needed by external services.
 *
 * Note that implementations of this interface should be considered thread safe.  Each call must return or throw
 * exceptions leaving the object in a consistent state.  This may be accomplished using locking.  All operations are to
 * be considered atomic unless otherwise specified.  Keep in mind that that while this instance may provided thread
 * safety, the specification for {@link Resource} is not.  Therefore, locking is necessary when performing operations
 * on individual {@link Resource} instances themselves.  This means that the internals of this service may lock the
 * individual {@link Resource} instances to perform work as well.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface ResourceService {

    /**
     * Called on start-up to ensure that the {@link ResourceService} has created and started any internal processes
     * that it may need to perform its work.
     */
    default void start() {};

    /**
     * Releases all memory associated with this {@link ResourceService}.  The actual action that happens here is
     * dependent on the specific implementation.  For in-memory implementations, this will simply close all resources
     * and exit.  For persistence-backed implementations this should flush all resources to disk before closing all.
     *
     * Once closed this instance may only be used after it has been restarted.
     */
    default void stop() {}

    /**
     * Without affecting acquisition or releases, this performs a simple check to see if the {@link Resource} with the
     * supplied {@link ResourceId} exists in this {@link ResourceService}.
     *
     * @param resourceId the {@link ResourceId}
     * @deprecated not used anywhere
     *
     * @return true if exists, false otherwise.
     */
    @Deprecated
    boolean exists(ResourceId resourceId);

    /**
     * Acquires a {@link Resource} guaranteeing it will remain in-memory until the resource is released.
     *
     * @return the {@link ResourceAcquisition}
     */
    ResourceAcquisition acquire(Path path);

    /**
     * Acquires a {@link Resource} guaranteeing it will remain in-memory until the resource is released.
     *
     * @return the {@link ResourceAcquisition}
     */
    ResourceAcquisition acquire(ResourceId resourceId);

    /**
     * Opens a {@link ResourceTransaction}.
     *
     * @return the {@link ResourceTransaction}
     */
    ResourceTransaction acquireWithTransaction(Path path);

    /**
     * Opens a {@link ResourceTransaction}.
     *
     * @return the {@link ResourceTransaction}
     */
    ResourceTransaction acquireWithTransaction(ResourceId resourceId);

    /**
     * Adds a {@link Resource} to this resource service.  This is used for the initial insert into the
     * {@link ResourceService}.  If linking to an additional {@link Path} is necessary, then the methods
     * {@link #linkPath(Path, Path)} or {@link #link(ResourceId, Path)} must be used to perform additional
     * aliasing operations.
     *
     * It is strongly recommended that newly inserted {@link Resource} instances be given a globally unique path
     * initially, which can be thought of as the primary path, and then subsequent aliases or links be maintained, even
     * if those particular {@link Path}s may collide.
     *
     * Once a {@link Resource} is passed ot this method, this {@link ResourceService} will take ownership of it.  This
     * means the the {@link Resource} may be closed after this call.  Therefore, subsequent operations may require the
     * {@link Resource} be fetched from serialization later for subsequent operations.
     *
     * {@see {@link #release(Resource)}}
     *
     * @param path the initial path for the {@link Resource}
     * @param resource the resource to insert
     *
     * @throws  DuplicateException if a resource is already present
     * @throws  IllegalArgumentException if the path is a wildcard path
     */
    void addAndReleaseResource(Path path, Resource resource);

    /**
     * Adds an acquires this {@link Resource}.  This is used for the initial insert into the {@link ResourceService}. If
     * linking to an additional {@link Path} is necessary, then the methods {@link #linkPath(Path, Path)} or
     * {@link #link(ResourceId, Path)} must be used to perform additional aliasing operations.
     *
     * It is strongly recommended that newly inserted {@link Resource} instances be given a globally unique path
     * initially, which can be thought of as the primary path, and then subsequent aliases or links be maintained, even
     * if those particular {@link Path}s may collide.
     *
     * This is only safe to use with freshly created {@link Resource} instances whose {@link ResourceId} has never
     * before been seen by the system.
     *
     * @param path
     * @param resource
     * @return the managed version of the supplied {@link Resource}
     */
    ResourceAcquisition addAndAcquireResource(Path path, Resource resource);

    /**
     * Returns a {@link Stream<ResourceId>} matching the provided {@link Path}.
     *
     * @param path the {@link Path} to match
     * @return a {@link Stream<ResourceId>}
     */
    Stream<Listing> listStream(Path path);

    /**
     * Given the provided {@link ResourceId}, this will create an additional alias for the provided destination
     * {@link Path}.
     *
     *
     * @param sourceResourceId
     * @param destination
     *
     * @throws  DuplicateException if an alias already exists for the destination
     * @throws  ResourceNotFoundException if the {@link Resource} can't be found
     */
    void link(ResourceId sourceResourceId, Path destination);

    /**
     * Given the provided {@link Path}, this will create an additional {@link Path} alias for the provided source.
     *
     * @param source the source {@link Path}
     * @param destination the destination {@link Path}
     */
    void linkPath(Path source, Path destination);

    /**
     * Similar to {@link #unlinkPath(Path, Consumer)}, however, this assumes that the final action should remove and
     * destroy the associated {@link Resource}.
     *
     * @param path a {@link Path} to unlink
     * @return true if the {@link Resource} associated with the {@link Path} was removed, false otherwise
     */
    default Unlink unlinkPath(Path path) {
        return unlinkPath(path, Resource::close);
    }

    /**
     * Unlinks the {@link Resource} for the provided {@link Path}.  If the unlinked {@link Path} is the final path, then
     * this will remove the {@link Path} from the {@link ResourceService}.  If this {@link Path} is the last alias for
     * the associated {@link Resource}, then the removed {@link ResourceId} will be passed to the supplied
     * {@link Consumer}.
     *
     * The supplied {@link Consumer} will not be called if there still exist aliases for the associated
     * {@link Resource}.  Only one {@link Resource} may be unlinked at a time.  Therefore, the supplied,
     * {@link Path} most not be a wildcard path.
     *
     * @param path a {@link Path} to unlink
     * @param removed a Consumer which will receive the removed {@link Resource}
     * @return true if the {@link Resource} associated with the {@link Path} was removed, false otherwise
     * @throws  IllegalArgumentException if the path is a wildcard path
     */
    Unlink unlinkPath(Path path, Consumer<Resource> removed);

    /**
     * Unlinks multiple {@link Resource}s.  This accepts a {@link Path}, which may be a wildcard.
     *
     * @param path
     * @param max
     * @return
     */
    default List<Unlink> unlinkMultiple(final Path path, int max) {
        final Logger logger = getLogger(getClass());
        return unlinkMultiple(path, max, r -> {
            try {
                r.close();
            } catch (Exception ex) {
                logger.error("Error closing resource.,", ex);
            }
        });
    }

    /**
     * Unlinks multiple {@link Resource}s.  This accepts a {@link Path}, which may be a wildcard.
     *
     * @param path the path to remove
     * @param max the maximum count to remove
     * @param removed a {@link Consumer<Resource>} to process each removal
     * @return the final {@link Unlink} operations
     */
    List<Unlink> unlinkMultiple(Path path, int max, Consumer<Resource> removed);

    /**
     * Removes a {@link Resource} instance from this resource service.
     *
     * @param resourceId the resourceId to the resource
     *
     * @throws  ResourceNotFoundException if no resource exists at that path
     * @throws  IllegalArgumentException if the path is a wildcard path
     */
    Resource removeResource(ResourceId resourceId);

    /**
     * Removes a {@link Resource} instance from this resource service.
     *
     * @param resourceIdString the path as a string
     * @return the removed {@link Resource}
     *
     * @throws  IllegalArgumentException if the path is a wildcard path
     *
     */
    default Resource removeResource(final String resourceIdString) {
        final ResourceId resourceId = resourceIdFromString(resourceIdString);
        return removeResource(resourceId);
    }

    /**
     * Removes all {@link Resource}s linked with the provided {@link Path}.
     *
     * @param path the {@link Path} for the resource.
     * @param max
     */
    default List<ResourceId> removeResources(final Path path, int max) {
        return removeResources(path, max, r -> r.close());
    }

    /**
     * Removes all {@link Resource}s linked with the provided {@link Path}.
     * @param path the the {@link Path} for the resource.
     * @param max
     * @param removed a {@link Consumer<Resource>} which accepts the removed resource
     */
    List<ResourceId> removeResources(final Path path, int max, final Consumer<Resource> removed);

    /**
     * Removes a {@link Resource} and then immediately closes it.
     *
     * @param resourceId
     * @throws  ResourceNotFoundException if no resource exists at that path
     * @throws  IllegalArgumentException if the path is a wildcard path
     */
    default void destroy(final ResourceId resourceId) {
        final var resource = removeResource(resourceId);
        if (resource != null) resource.close();
    }

    /**
     * Removes a {@link Resource} and then immediately closes it.
     *
     * @param resourceIdString the {@link String} representation of a {@link ResourceId}
     * @throws  ResourceNotFoundException if no resource exists at that path
     * @throws  IllegalArgumentException if the path is a wildcard path
     */
    default void destroy(final String resourceIdString) {
        final Resource resource = removeResource(resourceIdString);
        resource.close();
    }

    /**
     * Destroys all {@link Resource}s at the provided {@link Path}.
     *
     * @param path the {@link Path}
     */
    default List<ResourceId> destroyResources(final Path path, final int max) {
        return removeResources(path, max, r -> r.close());
    }

    /**
     * Removes all resources from.  The returned {@link Stream<Resource>} returns any {@link Resource}s that are still
     * occupying memory and must be closed.  The returned stream may be empty if all have been persisted.  This
     * operation may lock the whole {@link ResourceService} to accomplish its task.
     */
    Stream<Resource> removeAllResources();

    /**
     * Removes all resources from the service and closes them.  Any exceptions encountered are logged and all resources
     * are attempted to be closed.
     */
    default void removeAndCloseAllResources() {
        final Logger logger = getLogger(getClass());
        removeAllResources().forEach(resource -> {
            try {
                resource.close();
            } catch (Exception ex) {
                logger.error("Error closing resource {}.", resource, ex);
            }
        });
    }

    long getInMemoryResourceCount();

    /**
     * Contains the association between the {@link Path} and {@link ResourceId}.
     */
    interface Listing {

        /**
         * Returns the {@link Path} of this {@link Listing}
         *
         * @return the {@link Path}
         */
        Path getPath();

        /**
         * Returns the {@link ResourceId} at the supplied {@link ResourceId}.
         *
         * @return the {@link ResourceId}
         */
        ResourceId getResourceId();

    }

    /**
     * The return-value for methods such as {@link #unlinkPath(Path)} and {@link #unlinkPath(Path, Consumer)}
     */
    interface Unlink {

        /**
         * Returns true if the {@link ResourceId} was destroyed as part of this unlink operation.
         *
         * @return true if destroyed, false otherwise
         */
        boolean isRemoved();

        /**
         * Returns the {@link ResourceId} at the supplied {@link ResourceId}.
         *
         * @return the {@link ResourceId}
         */
        ResourceId getResourceId();

    }

    /**
     * Acquires a {@link Resource}, guaranteeing it will stay in memory until this object is closed. This does not
     * provide access to the {@link Resource}. Use a {@link ResourceTransaction} for that purpose to guarantee
     * synchronized access to the resource.
     */
    interface ResourceAcquisition extends AutoCloseable {

        /**
         * Gets the {@link ResourceId}.
         *
         * @return the {@link ResourceId}
         */
        ResourceId getResourceId();

        /**
         * Closes the transaction
         */
        void close();

        /**
         * Creates a new {@link ResourceTransaction} from this acquisition.
         *
         * @return the {@link ResourceTransaction}
         */
        ResourceTransaction begin();

    }

    /**
     * Represents a transaction against a single instance of a {@link Resource}. When accessing a {@link Resource}, this
     * ensures a write-lock on the resource as well as all modifications it may make therein.
     */
    interface ResourceTransaction extends AutoCloseable {

        /**
         * Gets the {@link Resource}.
         *
         * @return the {@link Resource}.
         */
        Resource getResource();

        /**
         * Commits the transaction.
         */
        void commit();

        /**
         * Rolls the transaction back.
         */
        void rollback();

        /**
         * Closes the transaction
         */
        void close();

    }

}
