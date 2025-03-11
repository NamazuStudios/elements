package dev.getelements.elements.rt.transact;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.cluster.id.ResourceId;

/**
 * Defines the data storage for underlying revision based data store.
 */
public interface DataStore {

    /**
     * Gets the index of tasks.
     * @return the {@link TaskIndex}
     */
    TaskIndex getTaskIndex();

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
     * Removes all {@link Resource} instances from the datastore. This operation will essentially clear the entire data
     * store, all revisions etc.
     */
    void removeAllResources(NodeId nodeId);

}
