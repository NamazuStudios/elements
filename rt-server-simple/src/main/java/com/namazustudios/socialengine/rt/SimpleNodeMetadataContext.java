package com.namazustudios.socialengine.rt;

import javax.inject.Inject;


public class SimpleNodeMetadataContext implements NodeMetadataContext {
    private LoadMonitorService loadMonitorService;

    private ResourceService resourceService;

    private Node node;

    private NodeId nodeId;

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
}
