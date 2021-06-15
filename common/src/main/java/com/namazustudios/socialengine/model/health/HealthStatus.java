package com.namazustudios.socialengine.model.health;

import java.util.List;
import java.util.Objects;

public class HealthStatus {

    private InstanceHealthStatus instanceStatus;

    private List<DatabaseHealthStatus> databaseStatus;

    public List<DatabaseHealthStatus> getDatabaseStatus() {
        return databaseStatus;
    }

    public void setDatabaseStatus(List<DatabaseHealthStatus> databaseStatus) {
        this.databaseStatus = databaseStatus;
    }

    public InstanceHealthStatus getInstanceStatus() {
        return instanceStatus;
    }

    public void setInstanceStatus(InstanceHealthStatus instanceStatus) {
        this.instanceStatus = instanceStatus;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HealthStatus{");
        sb.append("instanceStatus=").append(instanceStatus);
        sb.append(", databaseStatus=").append(databaseStatus);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthStatus that = (HealthStatus) o;
        return Objects.equals(getInstanceStatus(), that.getInstanceStatus()) && Objects.equals(getDatabaseStatus(), that.getDatabaseStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInstanceStatus(), getDatabaseStatus());
    }

}
