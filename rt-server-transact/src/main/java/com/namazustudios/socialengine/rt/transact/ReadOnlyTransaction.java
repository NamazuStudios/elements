package com.namazustudios.socialengine.rt.transact;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceService;
import com.namazustudios.socialengine.rt.ResourceService.Listing;
import com.namazustudios.socialengine.rt.exception.ResourceNotFoundException;
import com.namazustudios.socialengine.rt.id.HasNodeId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Represents a snapshot of the current database at a particular {@link Revision}. This guarantees that the data must
 * exist for the life of this transaction at the supplied {@link Revision}. Once closed, however, the {@link Revision}
 * may be collected by the database.
 */
public interface ReadOnlyTransaction extends AutoCloseable {

    /**
     * Returns the {@link NodeId} of this {@link ReadOnlyTransaction}.
     *
     * @return the {@link NodeId}
     */
    NodeId getNodeId();

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
     * The supplied {@link ReadableByteChannel} is only guaranteed to be valid for the life of the transaction which
     * created it. It may still be possible to write after the transaction is closed. However, the behavior of which
     * is not defined.
     *
     * @param resourceId the {@link ResourceId}
     * @return a {@link ReadableByteChannel} with the contents of the {@link Resource}
     */
    ReadableByteChannel loadResourceContents(ResourceId resourceId) throws IOException;

    /**
     * Closes this transaction, releasing any underlying system resources associated with this transaction.
     *
     */
    void close();

    /**
     * Checks that this transaction's {@link NodeId} matches the supplied {@link Path}. If the {@link Path} has a
     * context, then the {@link Path} is presumed to match as the {@link NodeId} will be inferred and returned.
     *
     * @param path the {@link Path}
     * @return the fully qualified {@link Path}
     */
    default Path check(final Path path) {
        return path.getOptionalNodeId()
            .map(pathNodeId -> {
                if (getNodeId().getInstanceId().equals(pathNodeId.getNodeId().getInstanceId())) return path;
                final String msg = format("%s %s have differing instance IDs.", getNodeId(), path);
                throw new IllegalArgumentException(msg);
            })
            .orElseGet(() -> path.toPathWithContext(getNodeId().asString()));
    }

    /**
     * Checks that this transaction's {@link NodeId} matches the supplied {@link NodeId}, which is to say that both
     * {@link NodeId} instances have the same {@link InstanceId}.
     *
     * @param owner the object owning the {@link NodeId} (used only for exception formatting)
     * @return the owner object, if the check succeeds
     */
    default <T extends HasNodeId> T check(final T owner) {
        if (getNodeId().getInstanceId().equals(owner.getNodeId().getInstanceId())) return owner;
        final String msg = format("%s %s have differing instance IDs.", getNodeId(), owner);
        throw new IllegalArgumentException(msg);
    }

}
