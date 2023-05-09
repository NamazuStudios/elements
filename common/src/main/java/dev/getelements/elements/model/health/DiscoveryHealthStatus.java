package dev.getelements.elements.model.health;

import java.util.List;
import java.util.Objects;

public class DiscoveryHealthStatus {

    private List<String> records;

    private List<String> knownHosts;

    public List<String> getRecords() {
        return records;
    }

    public void setRecords(List<String> records) {
        this.records = records;
    }

    public List<String> getKnownHosts() {
        return knownHosts;
    }

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
