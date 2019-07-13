package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;

import javax.inject.Inject;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * Provides data for an Instance.
 */
public class JeroMQInstanceMetadataContext implements InstanceMetadataContext {

    private InstanceId instanceId;

    private Set<Node> nodeSet;

    private LoadMonitorService loadMonitorService;

    @Override
    public Set<NodeId> getNodeIds() {
        return getNodeSet().stream().map(n -> n.getNodeId()).collect(toSet());
    }

    @Override
    public double getLoadAverage() {
        return getLoadMonitorService().getLoadAverage();
    }

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Inject
    public void setInstanceId(InstanceId instanceId) {
        this.instanceId = instanceId;
    }

    public Set<Node> getNodeSet() {
        return nodeSet;
    }

    @Inject
    public void setNodeSet(Set<Node> nodeSet) {
        this.nodeSet = nodeSet;
    }

    public LoadMonitorService getLoadMonitorService() {
        return loadMonitorService;
    }

    @Inject
    public void setLoadMonitorService(LoadMonitorService loadMonitorService) {
        this.loadMonitorService = loadMonitorService;
    }

}
