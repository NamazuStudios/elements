package com.namazustudios.socialengine.model.health;

import java.util.List;
import java.util.Objects;

public class HealthStatus {

    public static double HEALTHY_THRESHOLD = 100.0;

    private int checksFailed;

    private int checksPerformed;

    private double overallHealth;

    private List<String> problems;

    private InstanceHealthStatus instanceStatus;

    private List<DatabaseHealthStatus> databaseStatus;

    private DiscoveryHealthStatus discoveryHealthStatus;

    private RoutingHealthStatus routingHealthStatus;

    private InvokerHealthStatus invokerHealthStatus;

    public List<String> getProblems() {
        return problems;
    }

    public void setProblems(List<String> problems) {
        this.problems = problems;
    }

    public int getChecksFailed() {
        return checksFailed;
    }

    public void setChecksFailed(int checksFailed) {
        this.checksFailed = checksFailed;
    }

    public int getChecksPerformed() {
        return checksPerformed;
    }

    public void setChecksPerformed(int checksPerformed) {
        this.checksPerformed = checksPerformed;
    }

    public double getOverallHealth() {
        return overallHealth;
    }

    public void setOverallHealth(double overallHealth) {
        this.overallHealth = overallHealth;
    }

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

    public DiscoveryHealthStatus getDiscoveryHealthStatus() {
        return discoveryHealthStatus;
    }

    public void setDiscoveryHealthStatus(DiscoveryHealthStatus discoveryHealthStatus) {
        this.discoveryHealthStatus = discoveryHealthStatus;
    }

    public RoutingHealthStatus getRoutingHealthStatus() {
        return routingHealthStatus;
    }

    public void setRoutingHealthStatus(RoutingHealthStatus routingHealthStatus) {
        this.routingHealthStatus = routingHealthStatus;
    }

    public InvokerHealthStatus getInvokerHealthStatus() {
        return invokerHealthStatus;
    }

    public void setInvokerHealthStatus(InvokerHealthStatus invokerHealthStatus) {
        this.invokerHealthStatus = invokerHealthStatus;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthStatus that = (HealthStatus) o;
        return getChecksFailed() == that.getChecksFailed() && getChecksPerformed() == that.getChecksPerformed() && Double.compare(that.getOverallHealth(), getOverallHealth()) == 0 && Objects.equals(getProblems(), that.getProblems()) && Objects.equals(getInstanceStatus(), that.getInstanceStatus()) && Objects.equals(getDatabaseStatus(), that.getDatabaseStatus()) && Objects.equals(getDiscoveryHealthStatus(), that.getDiscoveryHealthStatus()) && Objects.equals(getRoutingHealthStatus(), that.getRoutingHealthStatus()) && Objects.equals(getInvokerHealthStatus(), that.getInvokerHealthStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getChecksFailed(), getChecksPerformed(), getOverallHealth(), getProblems(), getInstanceStatus(), getDatabaseStatus(), getDiscoveryHealthStatus(), getRoutingHealthStatus(), getInvokerHealthStatus());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HealthStatus{");
        sb.append("checksFailed=").append(checksFailed);
        sb.append(", checksPerformed=").append(checksPerformed);
        sb.append(", overallHealth=").append(overallHealth);
        sb.append(", problems=").append(problems);
        sb.append(", instanceStatus=").append(instanceStatus);
        sb.append(", databaseStatus=").append(databaseStatus);
        sb.append(", discoveryHealthStatus=").append(discoveryHealthStatus);
        sb.append(", routingHealthStatus=").append(routingHealthStatus);
        sb.append(", invokerHealthStatus=").append(invokerHealthStatus);
        sb.append('}');
        return sb.toString();
    }

}
