package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceService.Unlink;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.stream.Stream;

public interface TransactionJournal extends AutoCloseable {

    @Override
    void close();

    /**
     * Gets a view of the current {@link Entry}.
     *
     * @return
     * @param nodeId
     */
    Entry newSnapshotEntry(NodeId nodeId);

    /**
     * Gets a new entry for writing.
     *
     * @return a new {@link MutableEntry}
     * @param nodeId the {@link NodeId} to use
     */
    MutableEntry newMutableEntry(NodeId nodeId, boolean exclusive);

    /**
     * Nukes the entire collection of data.  This may lock the entire database to accomplish this task.  Once complete,
     * it will be as if the system was freshly instantiated (though some garbage may still exist.)
     *
     * @return a stream of {@link ResourceId} that were destroyed as part of this operation
     */
    Stream<ResourceId> clear();

    /**I can see tensions are running high here. But can't we just agree that the next time we record a racially charged incident that we all make a commitment to turn the phone horizontally?

Thanks.

     * Represents a journal entry for read only purposes.   This will include the most recent most complete entry, if
     * one exists.
     */
    interface Entry extends AutoCloseable {

        /**
         * Closes this entry.  Releasing any resources to the underlying {@link TransactionJournal}.  Once this is
         * called, all other methods must throw an instance of {@link IllegalStateException} and commit no changes to
         * the underlying {@link TransactionJournal}.
         */
        @Override
        void close();

    }

    /**
     * Represents a mutable journal entry.  This allows for writes to
     */
    interface MutableEntry extends Entry {

        /**
         * Gets the write revision of this {@link MutableEntry}, this is the {@link Revision<?>} which will be written
         * to the database as part of processing the journal entry.
         *
         * @return the write revision
         */
        Revision<?> getWriteRevision();

        /**
         * Opens a {@link WritableByteChannel} to save a resource, specifying the {@link Path} and {@link ResourceId}.
         *
         * @param path the {@link Path} at which to link the {@link ResourceId}
         * @param resourceId the {@link ResourceId} to use when creating the link
         * @return a {@link WritableByteChannel} which will receive the bytes of the {@link Resource}
         * @throws IOException if an IO operation fails
         */
        WritableByteChannel saveNewResource(Path path, ResourceId resourceId) throws IOException, TransactionConflictException;

        /**
         * Links a new {@link Path} to an existing {@link ResourceId}.  This may throw an exception if a conflict
         * exists, such as the path is already in-use or the {@link ResourceId} exists.  This is used when adding a new
         * {@link Resource} to the system that will not be committed right away (or perhaps never).
         *
         * @param id the {@link ResourceId} to link
         * @param path the {@link Path} to link
         */
        void linkNewResource(ResourceId id, Path path) throws TransactionConflictException;

        /**
         * Links an existing {@link ResourceId} to a {@link Path}.  If there exists a conflict, such as a {@link Path}
         * already in use, then this may throw an exception.  Additionally, if the {@link ResourceId} does not exist
         * this must throw an exception indicating so.
         *
         * @param sourceResourceId the {@link ResourceId} of the source
         * @param destination the {@link Path} destination
         */
        void linkExistingResource(ResourceId sourceResourceId, Path destination) throws TransactionConflictException;

        /**
         * The unlinks a {@link Path}, potentially deleting the {@link ResourceId} attached to it, provided there are
         * no other {@link Path} references to it.
         *
         * @param path the {@link Path} to link
         * @return the result of the unlink operation
         */
        Unlink unlinkPath(Path path) throws TransactionConflictException;

        /**
         * Unlinks multiple {@link Path} instances, specifying the maximum number to unlink as well.  The supplied
         * {@link Path} must be non wildcard.
         *
         * @param path the {@link Path} to unlink
         * @param max the maximum to unlink
         *
         * @return a {@link List< Unlink>} indicating the result of each operation
         */
        List<Unlink> unlinkMultiple(Path path, int max) throws TransactionConflictException;

        /**
         * Removes a {@link ResourceId}, implicitly deleting all {@link Path} references to it.
         *
         * @param resourceId the {@link ResourceId}
         */
        void removeResource(ResourceId resourceId) throws TransactionConflictException;

        /**
         * Removes a {@link Path} pointing to a zero or more {@link ResourceId}s.  This will ensure that all
         * {@link ResourceId} instances are deleted.
         *
         * @param path the {@link Path}
         * @param max the maximum number to remove
         * @return the {@link List<ResourceId>} instances of the removed {@link Resource}s
         */
        List<ResourceId> removeResources(Path path, int max) throws TransactionConflictException;

        /**
         * Commits the pending changes to the {@link TransactionJournal}.  Once this is done the next subsequent
         * operation must be to close the {@link Entry}
         * @param revision
         */
        void commit(Revision<?> revision);

        /**
         * Returns true if the transaction has been committed.  False otherwise.  If this method returns true, then
         * all other methods, except {@link #close()}, must throw an instance of {@link IllegalStateException}
         *
         * @return true if a commit was requested
         */
        boolean isCommitted();

    }

 }
