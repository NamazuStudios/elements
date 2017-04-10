package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;

import java.util.Iterator;
import java.util.function.Supplier;

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
     * Gets all resources in this service.  Note that the {@link Iterator} returned
     * by this {@link Iterable} must support the {@link Iterator#remove()} operation
     * and will properly call {@link Resource#onRemove(Path)} appropraitely.
     *
     * @return all of the resources.
     */
    Iterable<ResourceT> getResources();

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
     * Gets all resources matching the given path.  This may be a single resource, or
     * if a wildcard path is specified, this will return all paths matching the given path.
     *
     * @param path the path
     * @return all of the resources matching the path
     */
    Iterable<ResourceT> getResources(Path path);

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
     * Moves the {@link Resource} at the given source path to the destination path.
     *
     * This throws an instance of {@link NotFoundException} if the resource path
     * is not found.
     *
     * @param source the resource path
     * @param destination the new destination path of the resource.
     *
     * @throws {@link NotFoundException} if no resource exists at that path
     * @throws {@link DuplicateException} if a resource at the destination path already exists
     * @throws {@link IllegalArgumentException} if either path is a wildcard path
     */
    void moveResource(Path source, Path destination);

    /**
     * Removes all resources from the service.
     */
    void removeAllResources();

    /**
     *
     * Removes a {@link ResourceT} instance from this resource service.
     *
     * @param path the path to the resource
     *
     * @throws {@l  ink NotFoundException} if no resource exists at that path
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    ResourceT removeResource(Path path);

    /**
     * Removes a {@link ResourceT} and then immediately closes it.
     *
     * @param path
     * @throws {@link NotFoundException} if no resource exists at that path
     * @throws {@link IllegalArgumentException} if the path is a wildcard path
     */
    void removeAndCloseResource(Path path);

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
