package com.namazustudios.socialengine.rt;

import javax.inject.Inject;


public class SimpleNodeMetadataContext implements NodeMetadataContext {
    private LoadMonitorService loadMonitorService;

    private ResourceService resourceService;

    private Node node;

    private NodeId nodeId;

    private InstanceUuidProvider instanceUuidProvider;

    @Override
    public void start() {
        getLoadMonitorService().start();
    }

    @Override
    public void stop() {
        getLoadMonitorService().stop();
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    @Override
    public long getInMemoryResourceCount() {
        return getResourceService().getInMemoryResourceCount();
    }

    @Override
    public double getLoadAverage() {
        return loadMonitorService.getLoadAverage();
    }


    public LoadMonitorService getLoadMonitorService() {
        return loadMonitorService;
    }

    @Inject
    public void setLoadMonitorService(LoadMonitorService loadMonitorService) {
        this.loadMonitorService = loadMonitorService;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public Node getNode() {
        return node;
    }

    @Inject
    public void setNode(Node node) {
        this.node = node;
        if (node != null) {
            nodeId = node.getNodeId();
        }
    }

    public InstanceUuidProvider getInstanceUuidProvider() {
        return instanceUuidProvider;
    }

    @Inject
    public void setInstanceUuidProvider(InstanceUuidProvider instanceUuidProvider) {
        this.instanceUuidProvider = instanceUuidProvider;
    }
}
