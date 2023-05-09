package dev.getelements.elements.rt.transact;

import dev.getelements.elements.rt.Path;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.rt.ResourceService.Unlink;
import dev.getelements.elements.rt.exception.ResourceNotFoundException;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.id.ResourceId;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.List;

public interface TransactionJournal {

    /**
     * Gets a new entry for writing.
     *
     * @return a new {@link MutableEntry}
     * @param nodeId the {@link NodeId} to use
     */
    MutableEntry newMutableEntry(NodeId nodeId);

    /**
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

        /**
         * Internal implementations use this to get the {@link Entry} as the original type. If the type cannot convert
         * to the requested type, this may throw an exception.
         *
         * @param originalType the original type
         * @param <EntryT>
         * @return
         */
        <EntryT extends Entry> EntryT getOriginal(final Class<EntryT> originalType);

    }

    /**
     * Represents a mutable journal entry.  This allows for writes to
     */
    interface MutableEntry extends Entry {

        /**
         * Opens a {@link WritableByteChannel} to save a resource, specifying the {@link Path} and {@link ResourceId}.
         *
         * This method expects the calling code to close the supplied {@link WritableByteChannel}.
         *
         * @param path the {@link Path} at which to link the {@link ResourceId}
         * @param resourceId the {@link ResourceId} to use when creating the link
         * @return a {@link WritableByteChannel} which will receive the bytes of the {@link Resource}
         * @throws IOException if an IO operation fails
         * @throws ResourceNotFoundException if no such resource exists
      is   */
        WritableByteChannel saveNewResource(Path path, ResourceId resourceId) throws IOException, TransactionConflictException;

        /**
         * Opens a {@link WritableByteChannel} to save the contents of an existing {@link Resource} specified by the
         * {@link ResourceId}.
         *
         * This method expects the calling code to close the supplied {@link WritableByteChannel}.
         *
         * @param resourceId the {@link ResourceId} of an existing {@link Resource} in the system
         * @return a {@link WritableByteChannel}
         * @throws ResourceNotFoundException if no such resource exists
         */
        WritableByteChannel updateResource(ResourceId resourceId) throws IOException, TransactionConflictException;

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
         * operation must be to close the {@link Entry}.
         *
         * @param revision
         */
        void commit(Revision<?> revision);

    }

 }
