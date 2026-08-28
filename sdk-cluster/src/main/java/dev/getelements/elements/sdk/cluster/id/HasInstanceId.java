package dev.getelements.elements.sdk.cluster.id;

import dev.getelements.elements.sdk.cluster.id.exception.InvalidInstanceIdException;
import dev.getelements.elements.sdk.cluster.id.exception.InvalidNodeIdException;

import java.util.Optional;

/**
 * Specifies a type which can be represented as, or can derive, an {@link InstanceId}.
 */
public interface HasInstanceId {

    /**
     * Returns a {@link InstanceId} for this object, throwing an exception if the {@link InstanceId} is not valid.  This
     * may return null to indicate that the {@link NodeId} is neither valid nor invalid, but rather simply not present.
     *
     * @return the {@link InstanceId}
     * @throws InvalidInstanceIdException if this instance is unable to derive a {@link InvalidInstanceIdException}
     */
    default InstanceId getInstanceId() throws InvalidInstanceIdException {
        return findInstanceId().orElseThrow(InvalidInstanceIdException::new);
    }

    /**
     * Return and {@link Optional<InstanceId>}. If this can't determine the node id, then this must return an empty instance
     * of {@link Optional}.  Unlinke {@link #getInstanceId())}, this makes no distinction between a missing and an invalid
     * id.
     *
     * @return an {@link Optional} of instance id
     */
    default Optional<InstanceId> findInstanceId() {
        try {
            final var instanceId = getInstanceId();
            return Optional.ofNullable(instanceId);
        } catch (InvalidNodeIdException ex) {
            return Optional.empty();
        }
    }

}
