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
    private UUID instanceUuid;
    private NodeId instanceNodeId;
    private Set<Node> nodeSet;
    private LoadMonitorService loadMonitorService;

    public UUID getInstanceUuid() {
        return getInstanceUuidProvider().get();
    }

    public Set<NodeId> getApplicationNodeIds() {
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

        if (instanceUuidProvider != null) {
            setInstanceUuid(instanceUuidProvider.get());
        }
        else {
            setInstanceUuid(null);
        }
    }

    private void setInstanceUuid(UUID instanceUuid) {
        this.instanceUuid = instanceUuid;

        if (instanceUuid != null) {
            final NodeId instanceNodeId = new NodeId(instanceUuid, null);
            setInstanceNodeId(instanceNodeId);
        }
        else {
            setInstanceNodeId(null);
        }
    }

    @Override
    public NodeId getInstanceNodeId() {
        return instanceNodeId;
    }

    private void setInstanceNodeId(NodeId instanceNodeId) {
        this.instanceNodeId = instanceNodeId;
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
