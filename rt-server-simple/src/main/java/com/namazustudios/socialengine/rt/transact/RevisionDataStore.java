package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;

/**
 * Defines the data storage for underlying revision based data store.
 */
public interface RevisionDataStore extends AutoCloseable {

    /**
     * Gets the {@link PathIndex} which manages the relationship between {@link Path}s with {@link ResourceId}s.
     *
     * @return the {@link PathIndex}
     */
    PathIndex getPathIndex();

    /**
     * Returns the {@link ResourceIndex} which manages the relationship betwen {@link ResourceId}s and their underlying
     * persistent storage.
     *
     * @return the {@link ResourceIndex}
     */
    ResourceIndex getResourceIndex();

    /**
     * Closes this {@link RevisionDataStore} releasing any resources associated with it such as file handles or
     * database connections.
     */
    void close();

}
