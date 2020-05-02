package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Monitor;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.ResourceService.Unlink;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Spliterator;
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
         * Closes this entry.  Releasing any resources to the underlying {@link TransactionJournal}.  Once this is
         * called, all other methods must throw an instance of {@link IllegalStateException} and commit no changes to
         * the underlying {@link TransactionJournal}.
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

    /**
     * Represents a mutable journal entry.  This allows for writes to
     */
    interface MutableEntry extends Entry {

        /**
         * Opens a {@link WritableByteChannel} to save a resource, specifying the {@link Path} and {@link ResourceId}.
         *
         * @param path the {@link Path} at which to link the {@link ResourceId}
         * @param resourceId the {@link ResourceId} to use when creating the link
         * @return a {@link WritableByteChannel} which will receive the bytes of the {@link Resource}
         * @throws IOException if an IO operation fails
         */
        WritableByteChannel saveNewResource(Path path, ResourceId resourceId) throws IOException;

        /**
         * Links a new {@link Path} to an existing {@link ResourceId}.  This may throw an exception if a conflict
         * exists, such as the path is already in-use or the {@link ResourceId} exists.  This is used when adding a new
         * {@link Resource} to the system that will not be committed right away (or perhaps never).
         *
         * @param id the {@link ResourceId} to link
         * @param path the {@link Path} to link
         */
        void linkNewResource(ResourceId id, Path path);

        /**
         * Links an existing {@link ResourceId} to a {@link Path}.  If there exists a conflict, such as a {@link Path}
         * already in use, then this may throw an exception.  Additionally, if the {@link ResourceId} does not exist
         * this must throw an exception indicating so.
         *
         * @param sourceResourceId the {@link ResourceId} of the source
         * @param destination the {@link Path} destination
         */
        void linkExistingResource(ResourceId sourceResourceId, Path destination);

        /**
         * The unlinks a {@link Path}, potentially deleting the {@link ResourceId} attached to it, provided there are
         * no other {@link Path} references to it.
         *
         * @param path the {@link Path} to link
         * @return the result of the unlink operation
         */
        Unlink unlinkPath(Path path);

        /**
         * Unlinks multiple {@link Path} instances, specifying the maximum number to unlink as well.  The supplied
         * {@link Path} must be non wildcard.
         *
         * @param path the {@link Path} to unlink
         * @param max the maximum to unlink
         *
         * @return a {@link List< Unlink>} indicating the result of each operation
         */
        List<Unlink> unlinkMultiple(Path path, int max);

        /**
         * Removes a {@link ResourceId}, implicitly deleting all {@link Path} references to it.
         *
         * @param resourceId the {@link ResourceId}
         */
        void removeResource(ResourceId resourceId);

        /**
         * Removes a {@link Path} pointing to a zero or more {@link ResourceId}s.  This will ensure that all
         * {@link ResourceId} instances are deleted.
         *
         * @param path the {@link Path}
         * @param max the maximum number to remove
         * @return the {@link List<ResourceId>} instances of the removed {@link Resource}s
         */
        List<ResourceId> removeResources(Path path, int max);

        /**
         * Commits the pending changes to the {@link TransactionJournal}.  Once this is done the next subsequent
         * operation must be to close the {@link Entry}
         */
        void commit();

        /**
         * Returns true if the transaction has been committed.  False otherwise.  If this method returns true, then
         * all other methods, except {@link #close()}, must throw an instance of {@link IllegalStateException}
         *
         * @return true if the transaction was committed
         */
        boolean isCommitted();
    }

 }
