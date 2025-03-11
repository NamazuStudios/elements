package dev.getelements.elements.sdk.model.health;

import java.util.List;
import java.util.Objects;

public class InstanceHealthStatus {

    private String instanceId;

    private List<String> nodeIds;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public List<String> getNodeIds() {
        return nodeIds;
    }

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
