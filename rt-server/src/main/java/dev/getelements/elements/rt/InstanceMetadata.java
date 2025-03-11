package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.cluster.id.NodeId;

import java.io.Serializable;
import java.util.Set;

public class InstanceMetadata implements Serializable {

    private double quality;

    private Set<NodeId> nodeIds;

    public double getQuality() {
        return quality;
    }

    public void setQuality(double quality) {
        this.quality = quality;
    }

    public Set<NodeId> getNodeIds() {
        return nodeIds;
    }

    public void setNodeIds(final Set<NodeId> nodeIds) {
        this.nodeIds = nodeIds;
    }

}
