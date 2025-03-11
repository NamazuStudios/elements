package dev.getelements.elements.rt.transact;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.ResourceEntry.OperationalStrategy;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Manages the index of paths to revisions {@link ResourceId}s as well as the inverse relationship.
 */
public interface PathIndex {

    /**
     * Cleans up whatever may have been left behind by a partial transaction.
     *
     * @param path the {@link Path}
     * @param transactionId the transaction ID
     */
    void cleanup(Path path, String transactionId);

    /**
     * Applies the change to the {@link ResourceIndex} according to the supplied transaction ID.
     *
     * @param path the {@link Path}
     * @param transactionId the transaction ID from which to apply
     */
    void applyChange(Path path, String transactionId);

    /**
     * Loads the {@link ResourceEntry} from the supplied {@link Path}. If non-existent then this will load a nascent
     * path entry.
     *
     * @param path
     * @return the {@link Path}
     */
    default Optional<ResourceEntry> findEntry(final Path path) {
        return findEntry(path, () -> new OperationalStrategy() {});
    }

    /**
     * Loads the {@link ResourceEntry} from the supplied {@link Path}. If non-existent then this will load a nascent
     * path entry.
     *
     * @return the {@link Path}
     */
    Optional<ResourceEntry> findEntry(Path path,
                                      Supplier<OperationalStrategy> operationalStrategy);

    /**
     * Lists all associations between {@link Path} and {@link ResourceId}s. This inclues listing of direct path links
     * as well as wildcard path links.
     *
     * @param path     the {@link Path}, may be wildcard if searching for multiple paths.
     * @return a {@link Stream<ResourceService.Listing>} instances
     */
    Stream<PathIndex.Listing> list(Path path);

    /**
     * Gets the {@link ResourceId} for the supplied path.
     *
     * @param path the {@link Path} to find
     * @return the {@link ResourceId} or {@link Optional#empty()} if no such resource exists at the path
     */
    default Optional<ResourceId> findResourceId(final Path path) {
        return findEntry(path).map(entry -> {
            try (var _e = entry) {
                return _e.getOriginalResourceId();
            }
        });
    }

    /**
     * Represents a listing of a {@link Path}.
     */
    interface Listing {

        /**
         * Gets the actual path matched in the hierarchy.
         *
         * @return the {@link Path}
         */
        Path getPath();

        /**
         * Opens a {@link ResourceEntry} for the record at the path.
         *
         * @return
         */
        default ResourceEntry open() {
            return open(new OperationalStrategy() {});
        }

        /**
         * Opens a {@link ResourceEntry} for the record at the path.
         *
         * @param operationalStrategy
         * @return
         */
        ResourceEntry open(OperationalStrategy operationalStrategy);

    }

}
