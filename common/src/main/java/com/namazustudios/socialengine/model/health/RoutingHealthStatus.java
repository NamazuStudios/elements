package com.namazustudios.socialengine.model.health;

import java.util.List;
import java.util.Objects;

public class RoutingHealthStatus {

    private String instanceId;

    private List<String> routingTable;

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public List<String> getRoutingTable() {
        return routingTable;
    }

    public void setRoutingTable(List<String> routingTable) {
        this.routingTable = routingTable;
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
        sb.append('}');
        return sb.toString();
    }

}
