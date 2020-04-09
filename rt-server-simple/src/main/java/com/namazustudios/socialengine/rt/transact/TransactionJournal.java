package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Monitor;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
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
     * Nukes the entire collection of data.  This may lock the entire database to accomplish this task.  Once complete,
     * it will be as if the system was freshly instantiated (though some garbage may still exist.)
     *
     * @return a stream of {@link ResourceId} that were destroyed as part of this operation
     */
    Stream<ResourceId> removeAllResources();

    /**
     * Returns a Monitor which is able to lock the entire journal.
     *
     * @return the exclusive monitor on the journal
     */
    Monitor getExclusiveMonitor();

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

        /**
         * Gets the {@link ResourceId} associated with the path, if it is available in the current journal entry.
         *
         * @param path the path to fetch.
         *
         * @return the {@link ResourceId} revision
         */
        Revision<ResourceId> getResourceId(Path path);

        /**
         * Opens a {@link ReadableByteChannel} to the contents of the {@link Resource} at the supplied {@link Path}
         * @param path the path to load
         * @return the revision of the {@link ReadableByteChannel} for the contents of the {@link Resource}
         * @throws IOException
         * @throws ResourceNotFoundException
         */
        Revision<ReadableByteChannel> loadResourceContents(Path path) throws IOException;

        /**
         * Opens a {@link ReadableByteChannel} to the contents of the {@link Resource} with the supplied
         * {@link ResourceId}.
         *
         * @param resourceId the respirce id to load
         * @return the revision of the {@link ReadableByteChannel} for the contents of the {@link Resource}
         * @throws IOException
         * @throws ResourceNotFoundException
         */
        Revision<ReadableByteChannel> loadResourceContents(ResourceId resourceId) throws IOException;

    }

    interface MutableEntry extends Entry {

        WritableByteChannel saveNewResource(Path path, ResourceId resourceId) throws IOException;

        void linkNewResource(Path path, ResourceId id);

        void linkExistingResource(ResourceId sourceResourceId, Path destination);

        ResourceService.Unlink unlinkPath(Path path);

        List<ResourceService.Unlink> unlinkMultiple(Path path, int max);

        void removeResource(ResourceId resourceId);

        List<ResourceId> removeResources(Path path, int max);

        void commit();

        boolean isCommitted();
    }

 }
