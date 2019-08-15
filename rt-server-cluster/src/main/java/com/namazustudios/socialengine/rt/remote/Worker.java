package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.Set;

/**
 * Represents the worker, hosing one or more nodes performing work.
 */
public interface Worker {

    /**
     * Gets a set of all active nodes.  This may change as new nodes may be introduced as well as removed.  The returned
     * {@link Set<NodeId>} shall be an instantaneous snapshot.  The returned set may be empty if no nodes are active.
     *
     * @return a {@link Set<NodeId>}
     */
    Set<NodeId> getActiveNodeIds();

}
