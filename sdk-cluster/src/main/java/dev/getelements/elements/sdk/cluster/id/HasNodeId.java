package dev.getelements.elements.sdk.cluster.id;

import dev.getelements.elements.sdk.cluster.id.exception.InvalidNodeIdException;

import java.util.Optional;

/**
 * An interface which may report a {@link NodeId}.
 */
public interface HasNodeId {

    /**
     * Returns a {@link NodeId} for this instance, throwing an exception if the {@link NodeId} is not valid.  This may
     * return null to indicate that the {@link NodeId} is neither valid nor invalid, but rather simply not present.
     *
     * @return the {@link NodeId} or null.
     * @throws InvalidNodeIdException if this instance is unable to derive a {@link NodeId}
     */
    NodeId getNodeId() throws InvalidNodeIdException;

    /**
     * Return and {@link Optional<NodeId>}. If this can't determine the node id, then this must return an empty instance
     * of {@link Optional}.  Unlinke {@link #getNodeId()}, this makes no distinction between a missing and an invalid
     * id.
     *
     * @return
     */
    default Optional<NodeId> getOptionalNodeId() {
        try {
            final NodeId nodeId = getNodeId();
            return Optional.ofNullable(nodeId);
        } catch (InvalidNodeIdException ex) {
            return Optional.empty();
        }
    }

}
