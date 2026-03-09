package dev.getelements.elements.sdk.model.health;

import java.util.List;
import java.util.Objects;

/** Represents the routing health status of an instance, including routing table entries. */
public class RoutingHealthStatus {

    /** Creates a new instance. */
    public RoutingHealthStatus() {}

    private String instanceId;

    private List<String> routingTable;

    private List<String> masterNodeRoutingTable;

    private List<String> applicationNodeRoutingTable;

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
     * Returns the general routing table entries.
     *
     * @return the routing table
     */
    public List<String> getRoutingTable() {
        return routingTable;
    }

    /**
     * Sets the general routing table entries.
     *
     * @param routingTable the routing table
     */
    public void setRoutingTable(List<String> routingTable) {
        this.routingTable = routingTable;
    }

    /**
     * Returns the master node routing table entries.
     *
     * @return the master node routing table
     */
    public List<String> getMasterNodeRoutingTable() {
        return masterNodeRoutingTable;
    }

    /**
     * Sets the master node routing table entries.
     *
     * @param masterNodeRoutingTable the master node routing table
     */
    public void setMasterNodeRoutingTable(List<String> masterNodeRoutingTable) {
        this.masterNodeRoutingTable = masterNodeRoutingTable;
    }

    /**
     * Returns the application node routing table entries.
     *
     * @return the application node routing table
     */
    public List<String> getApplicationNodeRoutingTable() {
        return applicationNodeRoutingTable;
    }

    /**
     * Sets the application node routing table entries.
     *
     * @param applicationNodeRoutingTable the application node routing table
     */
    public void setApplicationNodeRoutingTable(List<String> applicationNodeRoutingTable) {
        this.applicationNodeRoutingTable = applicationNodeRoutingTable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutingHealthStatus that = (RoutingHealthStatus) o;
        return Objects.equals(instanceId, that.instanceId) && Objects.equals(routingTable, that.routingTable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceId, routingTable);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RoutingHealthStatus{");
        sb.append("instanceId='").append(instanceId).append('\'');
        sb.append(", routingTable=").append(routingTable);
        sb.append(", masterRoutingTable=").append(masterNodeRoutingTable);
        sb.append(", applicationNodeRoutingTable=").append(applicationNodeRoutingTable);
        sb.append('}');
        return sb.toString();
    }

}
