package com.namazustudios.socialengine.rt.id;

import com.namazustudios.socialengine.rt.exception.InvalidNodeIdException;

/**
 * An interface which may report a {@link NodeId}.
 */
public interface HasNodeId {

    /**
     * Returns a {@link NodeId} for this instance, throwing an exception if the {@link NodeId} is not valid.  This may
     * return null to indicate that the {@link NodeId} is neither valid nor invalid, but rather simply not present.
     *
     * A {@link NodeId} may be invalid if the
     *
     * @return the {@link NodeId} or null.
     * @throws InvalidNodeIdException if this instance is unable to derive a {@link NodeId}
     */
    NodeId getNodeId() throws InvalidNodeIdException;

}
