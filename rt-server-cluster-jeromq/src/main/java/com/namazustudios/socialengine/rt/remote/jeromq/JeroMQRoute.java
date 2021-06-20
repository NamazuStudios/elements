package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.Objects;

import static java.lang.String.format;

public class JeroMQRoute implements Comparable<JeroMQRoute> {

    private final NodeId nodeId;

    private final InstanceId instanceId;

    private final String instanceConnectAddress;

    public JeroMQRoute(final NodeId nodeId, final String instanceConnectAddress) {
        this.nodeId = nodeId;
        this.instanceId = nodeId.getInstanceId();
        this.instanceConnectAddress = instanceConnectAddress;
    }

    public NodeId getNodeId() {
        return nodeId;
    }

    public InstanceId getInstanceId() {
        return instanceId;
    }

    public String getInstanceConnectAddress() {
        return instanceConnectAddress;
    }

    public String getLocalBindAddress() {
        return format("inproc://mux/%s?%s", nodeId.asString(), instanceConnectAddress);
    }

    public boolean matches(final InstanceId instanceId, final String instanceConnectAddress) {
        return this.instanceId.equals(instanceId) && this.instanceConnectAddress.equals(instanceConnectAddress);
    }

    @Override
    public String toString() {
        return format("%s -> %s", nodeId, instanceConnectAddress);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JeroMQRoute that = (JeroMQRoute) o;
        return Objects.equals(nodeId, that.nodeId) && Objects.equals(instanceConnectAddress, that.instanceConnectAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, instanceConnectAddress);
    }

    @Override
    public int compareTo(final JeroMQRoute o) {
        int cmp;
        return (cmp = nodeId.compareTo(o.nodeId)) == 0
            ? instanceConnectAddress.compareTo(o.instanceConnectAddress)
            : cmp;
    }

}
