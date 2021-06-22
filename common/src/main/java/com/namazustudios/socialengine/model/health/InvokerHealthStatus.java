package com.namazustudios.socialengine.model.health;

import java.util.List;
import java.util.Objects;

public class InvokerHealthStatus {

    private List<String> connectedPeers;

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
        return Objects.equals(getConnectedPeers(), that.getConnectedPeers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConnectedPeers());
    }
}
