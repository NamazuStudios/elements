package dev.getelements.elements.rt.transact;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.exception.DuplicateException;
import dev.getelements.elements.rt.exception.ResourceNotFoundException;
import dev.getelements.elements.sdk.cluster.id.ResourceId;

import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Represents a loaded and limited snapshot of the database. In order to provide fine-grained access, this specifies a
 * subset of {@link Path} and {@link ResourceId} instances which must be locked. Changes to the {@link Snapshot} only
 * happen in memory and do not write data to disk.
 */
public interface Snapshot extends AutoCloseable {

    /**
     * List all {@link ResourceEntry} instances matching the {@link Path}. As a {@link Snapshot} may be backed by a
     * {@link Stream} from the {@link DataStore}, it is required that the Stream reach a terminal state or be explicitly
     * closed.
     *
     * Closing this {@link Snapshot} will close the stream associatd with it.
     *
     * @param path the path
     * @return the {@link Stream<  ResourceEntry  >}
     */
    Stream<ResourceService.Listing> list(Path path);

    /**
     * Finds the {@link TaskEntry} associated with the supplied {@link ResourceId}.
     *
     * @param resourceId the {@link ResourceId}
     * @return the {@link Optional} containing the {@link TaskEntry<ResourceId>}
     */
    Optional<TaskEntry<ResourceId>> findTaskEntry(ResourceId resourceId);

    /**
     * Gets the {@link TaskEntry} or throws an instance of {@link SnapshotMissException}.
     *
     * @param resourceId the path
     * @return the {@link Optional} containing a {@link TaskEntry} never null
     */
    TaskEntry<ResourceId> getOrCreateTaskEntry(ResourceId resourceId);

    /**
     * Gets the {@link ResourceEntry} or throws an instance of {@link SnapshotMissException}.
     *
     * @param path the Resource id
     * @return the {@link ResourceEntry}, never null
     */
    default ResourceEntry getResourceEntry(final Path path) {
        return findResourceEntry(path).orElseThrow(() -> {
            final var message = format("No resource exists at path %s",path);
            return new ResourceNotFoundException(message);
        });
    }

    /**
     * Finds the {@link ResourceEntry} at the supplied path.
     *
     * @param path the path
     * @return an {@link Optional} containing the Entry.
     */
    Optional<ResourceEntry> findResourceEntry(Path path);

    /**
     * Gets the {@link ResourceEntry} or throws an instance of {@link SnapshotMissException}.
     *
     * @param resourceId the Resource id
     * @return the {@link ResourceEntry}, never null
     * @throws SnapshotMissException if the snapshot entry was never loaded
     */
    default ResourceEntry getResourceEntry(final ResourceId resourceId) {
        return findResourceEntry(resourceId).orElseThrow(() -> {
            final var message = format("No resource exists with id %s", resourceId);
            return new ResourceNotFoundException(message);
        });
    }

    /**
     * Finds the {@link ResourceEntry} at the supplied ResourceId.
     *
     * @param resourceId the path
     * @return an {@link Optional} containing the Entry.
     */
    Optional<ResourceEntry> findResourceEntry(ResourceId resourceId);

    /**
     * Gets all {@link TaskEntry} instances in this {@link Snapshot}.
     *
     * @return gets all {@link TaskEntry} instances associated with this {@link Snapshot}
     */
    Collection<TaskEntry<?>> getTaskEntries();

    /**
     * Gets all {@link ResourceEntry} instances in this {@link Snapshot}.
     *
     * @return gets all {@link ResourceEntry} instances associated with this {@link Snapshot}
     */
    Collection<ResourceEntry> getResourceEntries();

    /**
     * Adds a new {@link ResourceEntry} with the supplied {@link ResourceId}, returning the result. Any previously accessed
     * {@link ResourceEntry} instances.
     *
     * @param resourceId the {@link ResourceId}
     * @return the {@link ResourceEntry} which was added.
     * @throws SnapshotMissException if the snapshot entry was never loaded
     * @throws DuplicateException if a {@link ResourceId} already exists
     */
    ResourceEntry add(ResourceId resourceId);

    /**
     * Closes this snapshot, releasing any underlying system resources associated with this snapshot.
     */
    void close();

    /**
     * Builds an instance of {@link Snapshot}.
     */
    interface Builder {

        /**
         * Loads the following {@link Path} in to this {@link Snapshot}.
         * @return this instance
         */
        Builder load(Path path);

        /**
         * Loads the specified {@link ResourceId} into this {@link Snapshot}.
         *
         * @return this instance
         */
        Builder load(ResourceId resourceId);

        /**
         * Builds the {@link Snapshot}.
         *
         * @return the {@link Snapshot}
         */
        Snapshot buildRO();

        /**
         * Builds the {@link Snapshot}.
         *
         * @return the {@link Snapshot}
         */
        Snapshot buildRW();

    }

}
