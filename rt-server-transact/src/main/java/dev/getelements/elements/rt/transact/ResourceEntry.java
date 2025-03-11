package dev.getelements.elements.rt.transact;

import dev.getelements.elements.rt.exception.DuplicateException;
import dev.getelements.elements.rt.exception.ResourceNotFoundException;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.sdk.cluster.path.Path;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Represents a node in the {@link Snapshot}. A Node contains all the known information of a Resource.
 */
public interface ResourceEntry extends AutoCloseable {

    /**
     * Returns true if the entry represents a present entry.
     *
     * @return true if nascent, false otherwise
     */
    default boolean isPresent() {
        return findResourceId().isPresent();
    }

    /**
     * Returns true if the entry represents an absent entry.
     *
     * @return true if absent, false otherwise
     */
    default boolean isAbsent() {
        return findResourceId().isEmpty();
    }

    /**
     * Return true if the content is the original content.
     * @return true if the content is original.
     */
    boolean isOriginalContents();

    /**
     * Checks if the list of reverse links are the original set of reverse links.
     * @return true if the links are original
     */
    boolean isOriginalReversePaths();

    /**
     * Gets the {@link ResourceId} or throws an instance of {@link ResourceNotFoundException} if no such resource
     * exists with this entry.
     *
     * @return the {@link ResourceId}, never null
     */
    default ResourceId getResourceId() {
        return findResourceId().orElseThrow(ResourceNotFoundException::new);
    }

    /**
     * Gets the {@link ResourceId} or throws an instance of {@link ResourceNotFoundException} if no such resource
     * exists with this entry.
     *
     * @return the {@link ResourceId}, never null
     */
    default ResourceId getOriginalResourceId() {
        return findOriginalResourceId().orElseThrow(ResourceNotFoundException::new);
    }

    /**
     * Finds the {@link ResourceId} associated with the node, or a result equivalent to {@link Optional#empty()} if
     * no such resource id exists.
     *
     * @return an {@link Optional<ResourceId>} containing the {@link ResourceId}
     */
    Optional<ResourceId> findResourceId();

    /**
     * Finds the {@link ResourceId} associated with the node, or a result equivalent to {@link Optional#empty()} if
     * no such resource id exists.
     *
     * @return an {@link Optional<ResourceId>} containing the {@link ResourceId}
     */
    default Optional<ResourceId> findOriginalResourceId() {
        return Optional.empty();
    }

    /**
     * Gets the internal immutable list of reverse links. Will always return an empty {@link Set} if no links exist.
     *
     * @return the reverse links, never null.
     */
    Set<Path> getReversePathsImmutable();

    /**
     * Gets the internal immutable list of reverse links. Will always return an empty {@link Set} if no links exist.
     *
     * @return the reverse links, never null.
     */
    default Set<Path> getOriginalReversePathsImmutable() {
        return emptySet();
    }

    /**
     * Loads the byte contents of the resource.
     *
     * @return the contents as a {@link ReadableByteChannel}
     * @throws NullResourceException if no contents exist for the resource.
     */
    default ReadableByteChannel loadResourceContents() throws IOException, NullResourceException {
        return findResourceContents().orElseThrow(NullResourceException::new).read();
    }

    /**
     * Finds the Resource contents.
     *
     * @return the {@link Optional} of {@link ResourceContents}
     */
    default Optional<ResourceContents> findResourceContents() {
        return findOriginalResourceContents();
    }

    /**
     * Finds the original Resource contents.
     *
     * @return the {@link Optional} of {@link ResourceContents}
     */
    default Optional<ResourceContents> findOriginalResourceContents() {
        return Optional.empty();
    }

    /**
     * Links this {@link ResourceEntry} for the supplied {@link Path}.
     *
     * @param path the {@link Path} to unlink
     * @return
     * @throws DuplicateException       if the entry is present at path
     * @throws IllegalArgumentException if the {@link Path} is a wildcard path
     * @throws SnapshotMissException    if the supplied {@link Path} is not in the current snapshot entry
     */
    boolean link(Path path);

    /**
     * Unlinks this {@link ResourceEntry} for the supplied {@link Path}.
     *
     * @param path the {@link Path} to unlink
     * @return true if the collection changed as the result of this operation
     * @throws IllegalStateException if the entry is nascent
     * @throws SnapshotMissException if the supplied {@link Path} is not in the current snapshot entry
     */
    boolean unlink(Path path);

    /**
     * Updates the contents of this {@link ResourceEntry} by overriding the supplied
     *
     * @return ResourceContents the contents
     */
    ResourceContents updateResourceContents();

    /**
     * Flags this entry for removal returning the entry to absent state. If this entry is absent this will have no
     * effect and the method will return false. Additionally, the operational strategy associated with this entry
     * may opt to do nothing and return false as well.
     *
     * @return true if clearing this entry had any effect
     */
    boolean delete();

    /**
     * Flushes a copy of this {@link ResourceEntry} to storage, identified with the supplied transaction ID.
     *
     * @param mutableEntry the transaction id
     */
    void flush(TransactionJournal.MutableEntry mutableEntry);

    /**
     * Closers this {@link ResourceEntry} releasing any underlying resources associated with the {@link ResourceEntry}.
     */
    default void close() {}

    /**
     * A Operational Strategy type for the supplied {@link ResourceEntry}. This performs the operations on the
     * underlying {@link ResourceEntry}.
     */
    interface OperationalStrategy {

        /**
         * Returns true if the content is the original content.
         *
         * @param entry the {@link ResourceEntry}
         *
         * @return true if the content is original
         */
        default boolean doIsOriginalContent(final ResourceEntry entry) {
            return true;
        }

        /**
         * Returns true if the content is the original content.
         *
         * @param entry the {@link ResourceEntry}
         *
         * @return true if the content is original
         */
        default boolean doIsOriginalReversePaths(final ResourceEntry entry) {
            return true;
        }

        /**
         * Finds the {@link ResourceId} for the {@link ResourceEntry}.
         *
         * @return the {@link Optional<ResourceId>} containing a {@link ResourceId}, or empty value
         */
        default Optional<ResourceId> doFindResourceId(final ResourceEntry entry) {
            return entry.findOriginalResourceId();
        }

        /**
         * Specifies how the {@link ResourceEntry} unlinks from the whole view of the snapshot.
         *
         * @param entry the entry
         * @param toLink the path to unlink
         * @return true if the linking was successful, false otherwise
         */
        default boolean doLink(final ResourceEntry entry, final Path toLink) {
            return false;
        }

        /**
         * Specifies how the {@link ResourceEntry} unlinks from the whole view of the snapshot.
         *
         * @param entry the entry
         * @param toUnlink the path to unlink
         * @return true if the unlinking was successful, false otherwise
         */
        default boolean doUnlink(final ResourceEntry entry, final Path toUnlink) {
            return false;
        }

        /**
         * Specifies how the {@link ResourceEntry} updates contents.
         */
        default ResourceContents doUpdateResourceContents(final ResourceEntry resourceEntry) {
            throw new UnsupportedOperationException("Unsupported Operation.");
        }

        /**
         * Specifies how the {@link ResourceEntry} loads contents.
         *
         * @param entry the entry
         * @return the {@link ReadableByteChannel}
         */
        default Optional<ResourceContents> doFindResourceContents(final ResourceEntry entry) {
            return entry.findOriginalResourceContents();
        }

        /**
         * Gets the immutable reverse links from the {@link ResourceEntry}
         *
         * @param entry the entry
         * @return a {@link Set<Path>} indicating the reverse linkags of this {@link ResourceEntry}
         */
        default Set<Path> doGetReversePathsImmutable(final ResourceEntry entry) {
            return entry.getOriginalReversePathsImmutable();
        }

        /**
         * Performs the operation to clear this entry and return it to nascent state.
         *
         * @param entry the entry to clear
         */
        default boolean doDelete(final ResourceEntry entry) {
            return false;
        }

    }

}
