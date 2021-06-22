package com.namazustudios.socialengine.model.health;

import java.util.List;
import java.util.Objects;

public class InvokerHealthStatus {

    private List<String> priorities;

    private List<String> connectedPeers;

    public List<String> getPriorities() {
        return priorities;
    }

    public void setPriorities(List<String> priorities) {
        this.priorities = priorities;
    }

    public List<String> getConnectedPeers() {
        return connectedPeers;
    }

    public void setConnectedPeers(List<String> connectedPeers) {
        this.connectedPeers = connectedPeers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvokerHealthStatus that = (InvokerHealthStatus) o;
        return Objects.equals(getPriorities(), that.getPriorities()) && Objects.equals(getConnectedPeers(), that.getConnectedPeers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPriorities(), getConnectedPeers());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("InvokerHealthStatus{");
        sb.append("status=").append(priorities);
        sb.append(", connectedPeers=").append(connectedPeers);
        sb.append('}');
        return sb.toString();
    }

}
