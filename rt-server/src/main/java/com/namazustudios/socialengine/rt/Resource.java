package com.namazustudios.socialengine.rt;

import java.util.Map;

/**
 * A {@link Resource} is a logical unit of work, which is represented by an instance of this type.  Though all
 * {@link Resource} isntances are designed to hold a particular it of logic, and many have functional similarities,
 * they differ in usage wildy.  The purpose of the {@link Resource} interface is to pool together the similarties
 * therein.
 *
 * A Resource is essentially a type that is capable primarily of both receiving {@link Request} instances
 * to produce {@link Response} instances.
 *
 * Once a resource is no longer needed, it is necessary to destroy the resource using the {@link AutoCloseable#close()}
 * method.
 *
 * Created by patricktwohig on 8/8/15.
 */
public interface Resource extends AutoCloseable {

    /**
     * Returns the immutable and globally-unique ID of this resource.  Though a resource
     * may exist at any path, this is the resource's ID.  All resources are assigned
     * a unique ID upon creation.  The ID must remain unique for the life of the
     * resource.
     *
     * @return the resource's ID
     */
    ResourceId getId();

    /**
     * Immediately following creation, code using this can pass these parameters to initialize the
     * resource.  This is completely defined by the resource.
     *
     * @param parameters the parameters
     */
    void init(final Map<String, Object> parameters);

    /**
     * Called when he resource has been added to the {@link ResourceService}.
     *
     * This method must be thread safe.
     *
     * @param path the path
     */
    void onAdd(Path path);

    /**
     * Called when the resource has been moved to a new path.  In the event
     * of an exception the {@link ResourceService} guarantees that the state
     * of the program remains consistent.
     *
     * This method must be thread safe.
     *
     * @param oldPath the old path
     * @param newPath the new path
     *
     */
    void onMove(Path oldPath, Path newPath);

    /**
     * Called when the resource has been removed by the {@link ResourceService}.
     *
     * This method must be thread safe.
     *
     * @param path the path
     */
    void onRemove(Path path);

    /**
     * Closes and destroys this Resource.  A resource, once destroyed, cannot
     * be used again.
     */
    void close();

    /**
     * Returns this resource's current path.
     *
     * @return the current path.
     */
    Path getCurrentPath();

}
