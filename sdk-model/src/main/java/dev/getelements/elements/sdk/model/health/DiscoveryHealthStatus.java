package dev.getelements.elements.sdk.model.health;

import java.util.List;
import java.util.Objects;

/** Represents the health status of the discovery service. */
public class DiscoveryHealthStatus {

    /** Creates a new instance. */
    public DiscoveryHealthStatus() {}

    private List<String> records;

    private List<String> knownHosts;

    /**
     * Returns the discovery records.
     *
     * @return the records
     */
    public List<String> getRecords() {
        return records;
    }

    /**
     * Sets the discovery records.
     *
     * @param records the records
     */
    public void setRecords(List<String> records) {
        this.records = records;
    }

    /**
     * Returns the known hosts.
     *
     * @return the known hosts
     */
    public List<String> getKnownHosts() {
        return knownHosts;
    }

    /**
     * Sets the known hosts.
     *
     * @param knownHosts the known hosts
     */
    public void setKnownHosts(List<String> knownHosts) {
        this.knownHosts = knownHosts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscoveryHealthStatus that = (DiscoveryHealthStatus) o;
        return Objects.equals(getRecords(), that.getRecords()) && Objects.equals(getKnownHosts(), that.getKnownHosts());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRecords(), getKnownHosts());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DiscoveryHealthStatus{");
        sb.append("records=").append(records);
        sb.append(", knownHosts=").append(knownHosts);
        sb.append('}');
        return sb.toString();
    }

}
