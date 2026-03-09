package dev.getelements.elements.sdk.model.health;

import java.util.List;
import java.util.Objects;

/** Represents the overall health status of the system. */
public class HealthStatus {

    /** Creates a new instance. */
    public HealthStatus() {}

    /** The threshold value above which the system is considered healthy. */
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

    /**
     * Returns the list of problems detected.
     *
     * @return the problems
     */
    public List<String> getProblems() {
        return problems;
    }

    /**
     * Sets the list of problems detected.
     *
     * @param problems the problems
     */
    public void setProblems(List<String> problems) {
        this.problems = problems;
    }

    /**
     * Returns the number of health checks that failed.
     *
     * @return the checks failed count
     */
    public int getChecksFailed() {
        return checksFailed;
    }

    /**
     * Sets the number of health checks that failed.
     *
     * @param checksFailed the checks failed count
     */
    public void setChecksFailed(int checksFailed) {
        this.checksFailed = checksFailed;
    }

    /**
     * Returns the number of health checks performed.
     *
     * @return the checks performed count
     */
    public int getChecksPerformed() {
        return checksPerformed;
    }

    /**
     * Sets the number of health checks performed.
     *
     * @param checksPerformed the checks performed count
     */
    public void setChecksPerformed(int checksPerformed) {
        this.checksPerformed = checksPerformed;
    }

    /**
     * Returns the overall health percentage.
     *
     * @return the overall health
     */
    public double getOverallHealth() {
        return overallHealth;
    }

    /**
     * Sets the overall health percentage.
     *
     * @param overallHealth the overall health
     */
    public void setOverallHealth(double overallHealth) {
        this.overallHealth = overallHealth;
    }

    /**
     * Returns the database health statuses.
     *
     * @return the database status list
     */
    public List<DatabaseHealthStatus> getDatabaseStatus() {
        return databaseStatus;
    }

    /**
     * Sets the database health statuses.
     *
     * @param databaseStatus the database status list
     */
    public void setDatabaseStatus(List<DatabaseHealthStatus> databaseStatus) {
        this.databaseStatus = databaseStatus;
    }

    /**
     * Returns the instance health status.
     *
     * @return the instance status
     */
    public InstanceHealthStatus getInstanceStatus() {
        return instanceStatus;
    }

    /**
     * Sets the instance health status.
     *
     * @param instanceStatus the instance status
     */
    public void setInstanceStatus(InstanceHealthStatus instanceStatus) {
        this.instanceStatus = instanceStatus;
    }

    /**
     * Returns the discovery health status.
     *
     * @return the discovery health status
     */
    public DiscoveryHealthStatus getDiscoveryHealthStatus() {
        return discoveryHealthStatus;
    }

    /**
     * Sets the discovery health status.
     *
     * @param discoveryHealthStatus the discovery health status
     */
    public void setDiscoveryHealthStatus(DiscoveryHealthStatus discoveryHealthStatus) {
        this.discoveryHealthStatus = discoveryHealthStatus;
    }

    /**
     * Returns the routing health status.
     *
     * @return the routing health status
     */
    public RoutingHealthStatus getRoutingHealthStatus() {
        return routingHealthStatus;
    }

    /**
     * Sets the routing health status.
     *
     * @param routingHealthStatus the routing health status
     */
    public void setRoutingHealthStatus(RoutingHealthStatus routingHealthStatus) {
        this.routingHealthStatus = routingHealthStatus;
    }

    /**
     * Returns the invoker health status.
     *
     * @return the invoker health status
     */
    public InvokerHealthStatus getInvokerHealthStatus() {
        return invokerHealthStatus;
    }

    /**
     * Sets the invoker health status.
     *
     * @param invokerHealthStatus the invoker health status
     */
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
