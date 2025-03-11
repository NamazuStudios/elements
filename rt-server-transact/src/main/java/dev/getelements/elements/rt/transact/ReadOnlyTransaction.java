package dev.getelements.elements.rt.transact;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.ResourceService.Listing;
import dev.getelements.elements.rt.exception.ResourceNotFoundException;
import dev.getelements.elements.sdk.cluster.id.HasNodeId;
import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.sdk.cluster.id.ResourceId;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Represents a read only transaction.
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
    Stream<ResourceService.Listing> list(Path path);

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
     * Checks that this transaction's {@link NodeId} matches the supplied {@link NodeId}, which is to say that both
     * {@link NodeId} instances have the same {@link InstanceId}.
     *
     * @param owner the object owning the {@link NodeId} (used only for exception formatting)
     * @return the owner object, if the check succeeds
     */
    default <T extends HasNodeId> T check(final T owner) {

        if (!getNodeId().equals(owner.getNodeId())) {
            final String msg = format("Node ids do not match (%s != %s)", getNodeId(), owner);
            throw new IllegalArgumentException(msg);
        }

        return owner;

    }

    interface Builder<TransactionT extends ReadOnlyTransaction> {

        Builder<TransactionT> with(Path path);

        Builder<TransactionT> with(ResourceId resourceId);

        TransactionT begin();

        default Builder<TransactionT> withPaths(final Iterable<Path> paths) {
            paths.forEach(this::with);
            return this;
        }

        default Builder<TransactionT> withResourceIds(final Iterable<ResourceId> resourceIds) {
            resourceIds.forEach(this::with);
            return this;
        }

    }

}
