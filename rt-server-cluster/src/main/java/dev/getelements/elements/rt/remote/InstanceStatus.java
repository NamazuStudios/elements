package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.id.InstanceId;
import dev.getelements.elements.rt.id.NodeId;

import java.util.List;

/**
 * Represents the instance status, which lists the
 */
public interface InstanceStatus {

    /**
     * Gets the {@link InstanceId} associated with this {@link InstanceStatus}
     *
     * @return the {@link InstanceId}
     */
    InstanceId getInstanceId();

    /**
     * Gets the node IDs known to the instance.
     *
     * @return the known {@link NodeId}s
     */
    List<NodeId> getNodeIds();

}
