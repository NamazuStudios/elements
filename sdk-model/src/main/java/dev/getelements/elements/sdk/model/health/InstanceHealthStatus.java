package dev.getelements.elements.sdk.model.health;

import java.util.List;
import java.util.Objects;

/** Represents the health status of a specific instance. */
public class InstanceHealthStatus {

    /** Creates a new instance. */
    public InstanceHealthStatus() {}

    private String instanceId;

    private List<String> nodeIds;

    /**
     * Returns the instance ID.
     *
     * @return the instance ID
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Sets the instance ID.
     *
     * @param instanceId the instance ID
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Returns the list of node IDs.
     *
     * @return the node IDs
     */
    public List<String> getNodeIds() {
        return nodeIds;
    }

    /**
     * Sets the list of node IDs.
     *
     * @param nodeIds the node IDs
     */
    public void setNodeIds(List<String> nodeIds) {
        this.nodeIds = nodeIds;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HealthInstanceStatus{");
        sb.append("instanceId='").append(instanceId).append('\'');
        sb.append(", nodeIds=").append(nodeIds);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceHealthStatus that = (InstanceHealthStatus) o;
        return Objects.equals(getInstanceId(), that.getInstanceId()) && Objects.equals(getNodeIds(), that.getNodeIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInstanceId(), getNodeIds());
    }

}
