package dev.getelements.elements.rt.transact;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.ResourceEntry.OperationalStrategy;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Allows for fetching and opening of the {@link Resource} data.
 */
public interface ResourceIndex {

    /**
     * Cleans up whatever may have been left behind by a partial transaction.
     *
     * @param transactionId the transaction ID
     */
    void cleanup(ResourceId resourceId, String transactionId);

    /**
     * Applies the change to the {@link ResourceIndex} according to the supplied transaction ID.
     *
     * @param resourceId the {@link ResourceId}
     * @param transactionId the transaction ID from which to apply
     */
    void applyContentsChange(ResourceId resourceId, String transactionId);

    /**
     * Applies the change to the {@link ResourceIndex} according to the supplied transaction ID.
     *
     * @param resourceId the {@link ResourceId}
     * @param transactionId the transaction ID from which to apply
     */
    void applyReversePathsChange(ResourceId resourceId, String transactionId);

    /**
     * Creates a new {@link ResourceEntry} with the supplied transaction id and {@link ResourceId}.
     *
     * @param resourceId          the resource id
     * @param operationalStrategy the operational strategy
     */
    ResourceEntry newEntry(ResourceId resourceId, Supplier<OperationalStrategy> operationalStrategy);

    /**
     * Creates a new {@link ResourceContents} for the supplied {@link ResourceId}.
     *
     * @param resourceId the contents
     * @return the {@link ResourceContents}
     */
    ResourceContents updateContents(ResourceId resourceId);

    /**
     * Loads the {@link ResourceEntry} from the supplied {@link Path}. If non-existent then this will load a nascent
     * path entry.
     *
     * @return the {@link Path}
     */
    default Optional<ResourceEntry> findEntry(final ResourceId resourceId) {
        return findEntry(resourceId, () -> new OperationalStrategy(){});
    }

    /**
     * Loads the {@link ResourceEntry} from the supplied {@link Path}. If non-existent then this will load a nascent
     * path entry.
     *
     * @return the {@link Path}
     */
    Optional<ResourceEntry> findEntry(ResourceId resourceId, Supplier<OperationalStrategy> operationalStrategy);

    /**
     * Finds reverse paths for the {@link ResourceId}.
     *
     * @param resourceId the {@link ResourceId}
     * @return the {@link Optional} set of reverse paths
     */
    default Optional<Set<Path>> findReversePaths(final ResourceId resourceId) {
        return findEntry(resourceId)
                .map(entry -> {
                    try (entry) {
                        return entry.getReversePathsImmutable();
                    }
                });
    }

    /**
     * Checks if a {@link ResourceId} exists.
     *
     * @param resourceId the {@link ResourceId}
     * @return true if it exists, false otherwise
     */
    default boolean exists(final ResourceId resourceId) {
        return findEntry(resourceId)
                .map(entry -> {
                    try (entry) {
                        return entry.isPresent();
                    }
                }).orElse(false);
    }

}
