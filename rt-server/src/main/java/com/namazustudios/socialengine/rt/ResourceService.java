package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;
import java.util.stream.Stream;

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
public interface ResourceService<ResourceT extends Resource> {

    /**
     * Gets a resource at the given path.
     *
     * @param path the path
     * @return the resource
     *
     * @throws {@link NotFoundException} if no resource exists at that path
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    ResourceT getResource(Path path);

    /**
     * Adds a {@link ResourceT} to this resource service.
     *
     * @param resource the resource
     *
     * @throws {@link DuplicateException} if a resource at the given path already exists
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    void addResource(Path path, ResourceT resource);

    /**
     * Adds a {@link ResourceT} to this resource service.  Rather than throwing an exception
     * if the resource already exists, this will return the existing instance instead.
     *
     * This returns either the newly added instance, or the instance that had already existed.
     *
     * Note that the given {@link Supplier<ResourceT>} should defer creation.  If you wish to
     * insert an existing resource, then consider using {@link #addResource(Path, ResourceT)}
     *
     * @param path the path for the {@link Resource} instance
     * @param resourceInitializer the resource initializer to use if the path is absent
     *
     * @return the actual {@link Resource} instance that was added
     *
     * @throws {@link DuplicateException} if a resource at the given path already exists
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    AtomicOperationTuple<ResourceT> addResourceIfAbsent(Path path, Supplier<ResourceT> resourceInitializer);

    /**
     * Removes a {@link ResourceT} instance from this resource service.
     *
     * @param path the path to the resource
     *
     * @throws {@l  ink NotFoundException} if no resource exists at that path
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    ResourceT removeResource(Path path);

    /**
     * Removes a {@link ResourceT} instance from this resource service.
     *
     * @param path the path as a string
     * @return the removed {@link Resource}
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    default ResourceT removeResource(String path) {
        return removeResource(new Path(path));
    }

    /**
     * Removes a {@link ResourceT} and then immediately closes it.
     *
     * @param path
     * @throws {@link NotFoundException} if no resource exists at that path
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    default void removeAndCloseResource(final Path path) {
        final ResourceT resource = removeResource(path);
        resource.close();
    }

    /**
     * Removes a {@link ResourceT} and then immediately closes it.
     *
     * @param path
     * @throws {@link NotFoundException} if no resource exists at that path
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    default void removeAndCloseResource(final String path) {
        final ResourceT resource = removeResource(path);
        resource.close();
    }

    /**
     * Removes all resources from the resource service.  The stream returned
     * must have already removed all resources.
     */
    Stream<ResourceT> removeAllResources();

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
     * Returend from the call to {@link #addResourceIfAbsent(Path, Supplier<ResourceT>)} to indicate
     * the status of the addition.
     *
     * @param <ResourceT> the resource type
     */
    interface AtomicOperationTuple<ResourceT> {

        /**
         * Returns true if th eresource was newly added.   False if the resource was existing.
         *
         * @return true, if newly added, false otherwise.
         */
        boolean isNewlyAdded();

        /**
         * Gets the resource that was either added, or the existing resource.
         *
         * @return the resource, never null
         */
        ResourceT getResource();

    }

}
