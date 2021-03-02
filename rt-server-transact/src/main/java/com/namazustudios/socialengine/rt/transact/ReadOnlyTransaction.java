package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.ResourceService.Listing;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.stream.Stream;

/**
 * Represents a snapshot of the current database at a particular {@link Revision}. This guarantees that the data must
 * exist for the life of this transaction at the supplied {@link Revision}. Once closed, however, the {@link Revision}
 * may be collected by the database.
 */
public interface ReadOnlyTransaction extends AutoCloseable {

    /**
     * Checks if the resource exists, returning false if it does not exist.
     *
     * @param resourceId the {@link ResourceId} to check
     *
     * @return true if the resource with the specified ID exists, false otherwise
     */
    boolean exists(ResourceId resourceId);

    /**
     * Returns a {@link Stream<Listing>} of all instances reading the current stream.  If nothing,
     * matches an empty stream is returned.
     *
     * @param path the path to check, may be direct or wildcard
     * @return a {@link Stream<Listing>}
     */
    Stream<Listing> list(Path path);

    /**
     * Gets the {@link ResourceId} associated with the given {@link Path}.
     *
     * @param path
     * @return
     * @@throws {@link ResourceNotFoundException} if there is no {@link ResourceId} associated with the path
     */
    ResourceId getResourceId(Path path);

    /**
     * Loads the contents of the {@link Resource} given the supplied {@link ResourceId}.  The supplied
     * {@link ReadableByteChannel} is only guaranteed to be valid for the life of this transaction. However, the caller
     * must not assume the transaction will automatically clean up open byte channels.
     *
     * If no resource exists with the supplied id, then this must throw an instance of
     * {@link ResourceNotFoundException} to indicate there is no resource in the persistent storage with that
     * particular {@link ResourceId}.
     *
     * @param resourceId the {@link ResourceId}
     * @return a {@link ReadableByteChannel} with the contents of the {@link Resource}
     */
    ReadableByteChannel loadResourceContents(ResourceId resourceId) throws IOException;

    /**
     * Closes this transaction, releasing any underlying system resources associated with this transaction.
     *
     * @throws TransactionConflictException if the operation caused a conflict on close.
     */
    void close();

}
