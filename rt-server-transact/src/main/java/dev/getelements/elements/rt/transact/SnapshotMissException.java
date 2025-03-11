package dev.getelements.elements.rt.transact;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.cluster.id.ResourceId;

import static java.lang.String.format;

/**
 * A special type of {@link InternalException} indicating that the {@link Snapshot} has no knowledge of a specified
 * {@link Path} or {@link ResourceId}. This exception type indicates that the internal state of the transactional data
 * store has encountered some inconsistency. Usually, the {@link Path} or {@link ResourceId} was not specified ahead
 * of time of the transaction.
 */
public class SnapshotMissException extends InternalException {

    public SnapshotMissException() {}

    public SnapshotMissException(String message) {
        super(message);
    }

    public SnapshotMissException(String message, Throwable cause) {
        super(message, cause);
    }

    public SnapshotMissException(Throwable cause) {
        super(cause);
    }

    public SnapshotMissException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SnapshotMissException(Path path) {
        this(format("No snapshot entry for: %s", path));
    }

    public SnapshotMissException(ResourceId resourceId) {
        this(format("No snapshot entry for: %s", resourceId));
    }

}
