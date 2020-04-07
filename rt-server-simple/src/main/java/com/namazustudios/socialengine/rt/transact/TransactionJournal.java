package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public interface TransactionJournal extends AutoCloseable {

    @Override
    void close();

    /**
     * Gets a view of the current {@link Entry}.
     *
     * @return
     */
    Entry getCurrentEntry();

    /**
     * Gets a new entry.
     *
     * @return
     */
    MutableEntry newEntry();

    /**
     * Represents a journal entry for read only purposes.   This will include the most recent most complete entry, if
     * one exists.
     */
    interface Entry extends AutoCloseable {

        /**
         * Gets the {@link Revision} of this entry.
         * @return
         */
        Revision<?> getRevision();

        /**
         * Closes this entry.  Committing any changes to the underlying journal if necessary.
         */
        @Override
        void close();

        /**
         * Checks that the supplied {@link ResourceId} exists for this journal entry.  If this journal entry has no
         * knowledge of the existence of this {@link ResourceId}.  For example, the specified {@link ResourceId} may
         * have been removed at the associated revision and not yet removed from the backing storage scheme.  Likewise,
         * the journal entry may contain the actual serialized bytes of the {@link Resource}, but it has not yet been
         * written to the disk in its expected location.
         *
         * @param resourceId the {@link ResourceId}
         * @return a {@link Revision<Boolean>} that, if present, indicates whether or not the {@link ResourceId} exists.
         */
        Revision<Boolean> exists(ResourceId resourceId);

        /**
         * Returns a {@link Spliterator<ResourceService.Listing>} for all matching resource IDs at the supplied
         * {@link Path}.
         *
         * @param path the path
         * @return the {@link Spliterator<ResourceService.Listing>} representing the resource IDs at the path.
         */
        Revision<Stream<ResourceService.Listing>> list(Path path);

    }

    interface MutableEntry extends Entry {}

 }
