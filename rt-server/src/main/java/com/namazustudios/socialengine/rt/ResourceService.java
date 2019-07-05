package com.namazustudios.socialengine.rt;


import com.namazustudios.socialengine.rt.exception.DuplicateException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
 * individual {@link Resource} instances to perform work as well.  {@see {@link ResourceLockService}}.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface ResourceService extends AutoCloseable {

    /**
     * Called on start-up to ensure that the {@link ResourceService} has created and started any internal processes
     * that it may need to perform its work.
     */
    default void start() {};

    /**
     * Without affecting acquisition or releases, this performs a simple check to see if the {@link Resource} with the
     * supplied {@link ResourceId} exists in this {@link ResourceService}.
     *
     * @param resourceId the {@link ResourceId}
     *
     * @return true if exists, false otherwise.
     */
    boolean exists(ResourceId resourceId);

    /**
     * Gets a {@link Resource} based on the resource ID.  The returned {@link Resource} is said to be acquired which
     * means it will not be serialized until no process has currently acquired the {@link Resource}.
     *
     * @param resourceId the {@link ResourceId}
     * @return the Resource, never null
     * @throws {@link ResourceNotFoundException} if no resource exists with that particular ID
     */
    Resource getAndAcquireResourceWithId(ResourceId resourceId);

    /**
     * Gets a resource at the given path.  The returned {@link Resource} is said to be acquired which means it will not
     * be serialized until no process has currently acquired the {@link Resource}.
     *
     * @param path the path the {@link Path}
     * @return the resource the {@link Resource}
     *
     * @throws {@link ResourceNotFoundException} if no resource exists at that path
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    Resource getAndAcquireResourceAtPath(Path path);

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
     * @throws {@link DuplicateException} if a resource is already present
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
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
     * Unlike {@link #addAndAcquireResource(Path, Resource)}, this will not immediately scheduleRelease the {@link Resource}
     * therefore forcing it to stay in memory until a subsequent call to {@link #release(Resource)} is made.  This is
     * useful for {@link Resource} instances that are short-lived and may never need to be serialized (such as those
     * {@link Resource}s used by the {@link HandlerContext}).
     *
     * @param path
     * @param resource
     * @return the managed version of the supplied {@link Resource}
     */
    Resource addAndAcquireResource(Path path, Resource resource);

    /**
     * Attempts to scheduleRelease ownership of the specified {@link Resource}, throwing an instance of
     * {@link ResourceNotFoundException} if the operation failed.
     *
     * @param resource the {@link Resource} to scheduleRelease
     */
    default void release(final Resource resource) {
        if (!tryRelease(resource)) {
            throw new ResourceNotFoundException("Resource not part of this ResourceService " + resource.getId());
        }
    }

    /**
     * Releases ownership of the {@link Resource} to this {@link ResourceService}.  Once released, any further operation
     * on the {@link Resource} is considered undefined behavior because this call may invoke {@link Resource#close()}.
     *
     * This does not guarantee that the {@link Resource} will be serialized.  It does however, make it a candidate
     * for serialization as soon as possible.  Usually when all other processes operating against the {@link Resource}
     * scheduleRelease their implementation.
     *
     * The default implementation of this does nothing as in-memory implementations do not need to implement this.  If
     * the {@link Resource} is not managed by this {@link ResourceService} then the behavior of this call is undefined.
     *
     * @param resource the {@link Resource} to add, must be first acquired by a call to this service.
     * @return true if released, false if the {@link Resource} does not exist in this {@link ResourceService}
     */
    default boolean tryRelease(final Resource resource) {
        return true;
    }

    /**
     * Provided the {@link Path}, this will list all {@link ResourceId}s matching the {@link Path}.  The supplied
     * {@link Path} may be a wildcard path, as determined by {@link Path#isWildcard()}, which will potentially match
     * many {@link ResourceId} instances.
     *
     * The returned {@link Collection<ResourceId>} will be read only, and the {@link Iterator#remove()} method will throw
     * an instance of {@link UnsupportedOperationException} if removal is attempted.
     *
     * @param path the {@link Path} to match
     * @return an {@link Iterable<ResourceId>} instances
     */
    Spliterator<Listing> list(Path path);

    /**
     * Returns a {@link Stream<ResourceId>} matching the provided {@link Path}.  The default implementation of this
     * simply relies on the {@link Spliterator<ResourceId>} returned by {@link #list(Path)}.
     *
     * @param path the {@link Path} to match
     * @return a {@link Stream<ResourceId>}
     */
    default Stream<Listing> listStream(Path path) {
        return StreamSupport.stream(list(path), false);
    }

    /**
     * Returns a parallel {@link Stream<ResourceId>} matching the provided {@link Path}.  The default implementation of
     * this simply relies on the {@link Spliterator<ResourceId>} returned by {@link #list(Path)}.
     *
     * @param path the {@link Path} to match
     * @return a parallel {@link Stream<ResourceId>}
     */
    default Stream<Listing> listParallelStream(Path path) {
        return StreamSupport.stream(list(path), true);
    }

    /**
     * Given the provided {@link ResourceId}, this will create an additional alias for the provided destination
     * {@link Path}.
     *
     *
     * @param sourceResourceId
     * @param destination
     *
     * @throws {@link DuplicateException} if an alias already exists for the destination
     * @throws {@link ResourceNotFoundException} if the {@link Resource} can't be found
     */
    void link(ResourceId sourceResourceId, Path destination);

    /**
     * Given the provided {@link Path}, this will create an additional {@link Path} alias for the provided source.
     *
     * @param source the source {@link Path}
     * @param destination the destination {@link Path}
     */
    default void linkPath(final Path source, final Path destination) {
        final Resource resource = getAndAcquireResourceAtPath(source);
        try {
            link(resource.getId(), destination);
        } finally {
            release(resource);
        }
    }

    /**
     * Similar to {@link #unlinkPath(Path, Consumer)}, however, this assumes that the final action should remove and
     * destroy the associated {@link Resource}.
     *
     * @param path a {@link Path} to unlink
     * @return true if the {@link Resource} associated with the {@link Path} was removed, false otherwise
     */
    default Unlink unlinkPath(Path path) {
        return unlinkPath(path, resource -> resource.close());
    }

    /**
     * Unlinks the {@link Resource} for the provided {@link Path}.  If the unlinked {@link Path} is the final path, then
     * this will remove the {@link Path} from the {@link ResourceService}.  If this {@link Path} is the last alias for
     * the associated {@link Resource}, then the removed {@link ResourceId} will be passed to the supplied
     * {@link Consumer<Resource>}.
     *
     * The supplied {@link Consumer<Resource>} will not be called if there still exist aliases for the associated
     * {@link Resource}.  Only one {@link Resource} may be unlinked at a time.  Therefore, the supplied,
     * {@link Path} most not be a wildcard path.
     *
     * @param path a {@link Path} to unlink
     * @param removed a Consumer<Resource> which will receive the removed {@link Resource}
     * @return true if the {@link Resource} associated with the {@link Path} was removed, false otherwise
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    Unlink unlinkPath(Path path, Consumer<Resource> removed);

    /**
     * Removes a {@link Resource} instance from this resource service.
     *
     * @param resourceId the path to the resource
     *
     * @throws {@l  ink ResourceNotFoundException} if no resource exists at that path
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    Resource removeResource(ResourceId resourceId);

    /**
     * Removes a {@link Resource} instance from this resource service.
     *
     * @param resoureIdString the path as a string
     * @return the removed {@link Resource}
     *
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     *
     */
    default Resource removeResource(final  String resoureIdString) {
        return removeResource(new ResourceId(resoureIdString));
    }

    /**
     * Removes a {@link Resource} and then immediately closes it.
     *
     * @param resourceId
     * @throws {@link ResourceNotFoundException} if no resource exists at that path
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    default void destroy(final ResourceId resourceId) {
        final Resource resource = removeResource(resourceId);
        resource.close();
    }

    /**
     * Removes a {@link Resource} and then immediately closes it.
     *
     * @param resourceIdString
     * @throws {@link ResourceNotFoundException} if no resource exists at that path
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    default void destroy(final String resourceIdString) {
        final Resource resource = removeResource(resourceIdString);
        resource.close();
    }

    /**
     * Removes all resources from.  The returned {@link Stream<Resource>} returns any {@link Resource}s that are still
     * occupying memory and must be closed.  The returned stream may be empty if all have been persisted.  This
     * operation may lock the whole {@link ResourceService} to accomplish its task.
     */
    Stream<Resource> removeAllResources();

    /**
     * Releases all memory associated with this {@link ResourceService}.  The actual action that happens here is
     * dependent on the specific implementation.  For in-memory implementations, this will simply close all resources
     * and exit.  For persistence-backed implementations this should flush all resources to disk before closing all.
     *
     * Once closed this instance may no longer be used.
     */
    @Override
    void close();

    /**
     * Removes all resources from the service and closes them.  Any exceptions encountered are logged and all resources
     * are attempted to be closed.
     */
    default void removeAndCloseAllResources() {
        final Logger logger = LoggerFactory.getLogger(getClass());
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
         * Returns the {@link ResourceId} at the supplied {@link ResourceId}.
         *
         * @return the {@link ResourceId}
         */
        ResourceId getResourceId();

        /**
         * Returns true if the {@link ResourceId} was destroyed as part of this unlink operation.
         *
         * @return true if destroyed, false otherwise
         */
        boolean isRemoved();

    }

}
