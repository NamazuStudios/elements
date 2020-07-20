package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.transact.TransactionJournal.Entry;
import com.namazustudios.socialengine.rt.transact.TransactionJournal.MutableEntry;

import java.util.stream.Stream;

/**
 * Defines the data storage for underlying revision based data store.
 */
public interface RevisionDataStore {

    /**
     * Locks the database revision guaranteeing that the revision will not be collected until the lock is released.
     * This method is safe to call from multiple threads, however the returned {@link LockedRevision} must be used from
     * only one thread at a time.
     *
     * @return the {@link LockedRevision} instance
     */
    LockedRevision lockCurrentRevision();

    /**
     * Begins the revision update process. This returns a {@link PendingRevisionChange} which represents the next
     * {@link Revision<?>} in the data store. This method is safe to call from multiple threads, however the returned
     * {@link PendingRevisionChange} must be used from only one thread at a time.
     *
     * @return the {@link PendingRevisionChange}
     */
    PendingRevisionChange beginRevisionUpdate();

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
     *
     * @return all resources.
     */
    Stream<ResourceId> removeAllResources();

    /**
     * Represents a locked revision. This guarantees that the revision will not be deleted by any garbage collection
     * process that the underlying data store implements until the revision is released.
     */
    interface LockedRevision extends AutoCloseable {

        /**
         * Gets the {@link Revision<?>} that is locked.
         *
         * @return the locked {@link Revision<?>}
         */
        Revision<?> getRevision();

        /**
         * Release the lock on the {@link Revision<?>}, making it potentially available for garbage collection.
         */
        @Override
        void close();

    }

    /**
     * Used to update a pending revision change. Once obtained, this will allow the calling code to either modify
     * the revision of hte data store, or fail the pending revision change. If neither option is used, then closing this
     * does nothing at all.
     */
    interface PendingRevisionChange extends LockedRevision {

        /**
         * Applies the {@link MutableEntry}'s pending changes to the datastore.
         *
         * @param entry the entry
         */
        void apply(Entry entry);

        /**
         * Performs any cleanup actions from the {@link MutableEntry}.
         *
         * @param entry
         */
        void cleanup(Entry entry);

        /**
         * Updates the {@link Revision<?>} of the data store, this should be performed after applying the entry to the
         * as it updates the current version to the associated {@link Revision<?>}
         */
        void update();

        /**
         * Marks the pending revision as failed and removes it from the pool of pending revisions. Before failing a
         * revision change, the calling code must be sure to clean up or unwind any partial changes to the system
         * before calling fail.
         */
        void fail();

    }

}
