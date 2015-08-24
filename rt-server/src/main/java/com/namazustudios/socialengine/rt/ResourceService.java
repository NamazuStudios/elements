package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.rt.edge.EdgeResource;

import java.util.Iterator;
import java.util.List;

/**
 * This is the service responsible for maintaining a set of {@link Resource} instances.  This
 * contains code to handle a the path hierarchy for the resources housed in the service.
 *
 * Resources can be added, moved, or deleted as needed by external services.
 *
 * Note that implementations of this interface should be considered thread
 * safe.  Each call must return or throw exceptions leaving the object in a consistent
 * state.  This may be accomplished using locking.
 *
 * Created by patricktwohig on 7/24/15.
 */
public interface ResourceService<ResourceT extends Resource> {

    /**
     * Gets all resources in this service.  Note that the {@link Iterator} returned
     * by this {@link Iterable} must support the {@link Iterator#remove()} operation
     * and will properly call {@link Resource#onRemove(String)} appropraitely.
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
     *
     */
    ResourceT getResource(String path);

    /**
     * Gets a single resource at the given path.
     *
     * @param pathComponents the path components
     * @return the resource
     *
     * @throws {@link NotFoundException} if no resource exists at that path
     *
     */
    ResourceT getResource(List<String> pathComponents);

    /**
     * Adds a {@link EdgeResource} to this resource service.
     *
     * @param resource the resource
     *
     * @throws {@link DuplicateException} if a resource at the given path already exists
     */
    void addResource(String path, ResourceT resource);

    /**
     * Moves the given resource to the given new destination.
     *
     * This throws an instance of {@link NotFoundException} if the resource path
     * is not found.
     *
     * @param source the resource path
     * @param destination the new destination path of the resource.
     *
     * @throws {@link NotFoundException} if no resource exists at that path
     * @throws {@link DuplicateException} if a resource at the destination path already exists
     *
     */
    void moveResource(String source, String destination);

    /**
     * Removes all resources from the service.
     */
    void removeAllResources();

    /**
     *
     * Removes a {@link EdgeResource} instance from this resource service.
     *
     * @param path the path to the resource
     *
     * @throws {@link NotFoundException} if no resource exists at that path
     *
     */
    ResourceT removeResource(String path);

}
