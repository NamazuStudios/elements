package com.namazustudios.socialengine.rt;


import com.namazustudios.socialengine.rt.exception.DuplicateException;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This is the service responsible for maintaining a set of {@link Resource} instances.  This
 * contains code to handle a the path hierarchy for the resources housed in the service.
 *
 * Resources can be added, moved, or deleted as needed by external services.
 *
 * Note that implementations of this interface should be considered thread
 * safe.  Each call must return or throw exceptions leaving the object in a consistent
 * state.  This may be accomplished using locking.  All operations are to be considered
 * atomic unless otherwise specified.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface ResourceService {

    /**
     * Gets a {@link Resource} based on the resource ID.
     *
     * @param resourceId
     * @return the Resource, never null
     * @throws {@link ResourceNotFoundException} if no resource exists with that particular ID
     */
    Resource getResourceWithId(ResourceId resourceId);

    /**
     * Gets a resource at the given path.
     *
     * @param path the path
     * @return the resource
     *
     * @throws {@link ResourceNotFoundException} if no resource exists at that path
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    Resource getResourceAtPath(Path path);

    /**
     * Adds a {@link Resource} to this resource service.  This is used for the initial insert into the
     * {@link ResourceService}.  If linking to an additiona {@link Path} is necessary, then the methods
     * {@link #linkPath(Path, Path)} or {@link #linkResourceId(ResourceId, Path)} must be used to perform additional
     * aliasing operations.
     *
     * It is strongly recommended that newly inserted {@link Resource} instances be given a globally unique path
     * initially, which can be thought of as the primary path, and then subsequent aliases or links be maintained, even
     * if those particular {@link Path}s may collide.
     *
     * @param path the initial path for the {@link Resource}
     * @param resource the resource to insert
     *
     * @throws {@link DuplicateException} if a resource is already present
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    void addResource(Path path, Resource resource);

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
    Spliterator<Listing> resourceIdsMatching(Path path);

    /**
     * Returns a {@link Stream<ResourceId>} matching the provided {@link Path}.  The default implementation of this
     * simply relies on the {@link Spliterator<ResourceId>} returned by {@link #resourceIdsMatching(Path)}.
     *
     * @param path the {@link Path} to match
     * @return a {@link Stream<ResourceId>}
     */
    default Stream<Listing> resourceIdsMatchingStream(Path path) {
        return StreamSupport.stream(resourceIdsMatching(path), false);
    }

    /**
     * Returns a parallel {@link Stream<ResourceId>} matching the provided {@link Path}.  The default implementation of
     * this simply relies on the {@link Spliterator<ResourceId>} returned by {@link #resourceIdsMatching(Path)}.
     *
     * @param path the {@link Path} to match
     * @return a parallel {@link Stream<ResourceId>}
     */
    default Stream<Listing> resourceIdsMatchingStreamParallel(Path path) {
        return StreamSupport.stream(resourceIdsMatching(path), true);
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
    void linkResourceId(ResourceId sourceResourceId, Path destination);

    /**
     * Given the provided {@link Path}, this will create an additional {@link Path} alias for the provided source.
     *
     * @param source the source {@link Path}
     * @param destination the destination {@link Path}
     */
    default void linkPath(Path source, Path destination) {
        final Resource resource = getResourceAtPath(source);
        linkResourceId(resource.getId(), destination);
    }

    /**
     * Similar to {@link #unlinkPath(Path, Consumer)}, however, this assumes that the final action should remove and
     * destroy the associated {@link Resource}.
     *
     * @param path a {@link Path} to unlink
     * @return true if the {@link Resource} associated with the {@link Path} was removed, false otherwise
     */
    default boolean unlinkPath(Path path) {
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
    boolean unlinkPath(Path path, Consumer<Resource> removed);

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
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
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
     * Removes all resources from the resource service.  The stream returned must have already removed all resources and
     * the supplied {@link Stream} is simply a view of {@link Resource} instances that have been removed.
     */
    Stream<Resource> removeAllResources();

    /**
     * Removes all resources from the service and closes them.  ANy exceptions
     * encountered are logged and all resources are attempted to be closed.
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

}
