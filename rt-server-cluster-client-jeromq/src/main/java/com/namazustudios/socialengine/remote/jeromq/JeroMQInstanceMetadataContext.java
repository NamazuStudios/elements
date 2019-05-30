package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.*;

import javax.inject.Inject;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Provides data for an Instance.
 */
public class JeroMQInstanceMetadataContext implements InstanceMetadataContext {
    private InstanceUuidProvider instanceUuidProvider;
    private Set<Node> nodeSet;
    private LoadMonitorService loadMonitorService;

    public UUID getInstanceUuid() {
        return getInstanceUuidProvider().get();
    }

    public Set<NodeId> getAllNodeIds() {
        return getNodeSet().stream().map(node -> node.getNodeId()).collect(Collectors.toSet());
    }

    public double getLoadAverage() {
        return getLoadMonitorService().getLoadAverage();
    }

    public InstanceUuidProvider getInstanceUuidProvider() {
        return instanceUuidProvider;
    }

    @Inject
    public void setInstanceUuidProvider(InstanceUuidProvider instanceUuidProvider) {
        this.instanceUuidProvider = instanceUuidProvider;
    }

    public Set<Node> getNodeSet() {
        return nodeSet;
    }

    @Inject
    /**
     * TODO: make sure this injection pattern is fine (borrowed from {@link com.namazustudios.socialengine.rt.MultiNodeContainer})
     */
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
