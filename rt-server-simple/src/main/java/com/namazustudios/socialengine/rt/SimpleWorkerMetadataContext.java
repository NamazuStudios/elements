package com.namazustudios.socialengine.rt;

import javax.inject.Inject;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.nameUUIDFromBytes;


public class SimpleWorkerMetadataContext implements WorkerMetadataContext {
    private LoadMonitorService loadMonitorService;

    private ResourceService resourceService;

    private Node node;

    private final WorkerId workerId;

    private InstanceUuidProvider instanceUuidProvider;

    public SimpleWorkerMetadataContext() {
        workerId = buildWorkerId();
    }

    private WorkerId buildWorkerId() {
        final UUID instanceUuid = getInstanceUuidProvider().get();

        final byte[] applicationUuidBytes = getNode().getId().getBytes(UTF_8);
        final UUID applicationUuid = nameUUIDFromBytes(applicationUuidBytes);

        final WorkerId workerId = new WorkerId(instanceUuid, applicationUuid);
        return workerId;
    }

    @Override
    public void start() {
        getLoadMonitorService().start();
    }

    @Override
    public void stop() {
        getLoadMonitorService().stop();
    }

    @Override
    public WorkerId getWorkerId() {
        return workerId;
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
    }

    public InstanceUuidProvider getInstanceUuidProvider() {
        return instanceUuidProvider;
    }

    @Inject
    public void setInstanceUuidProvider(InstanceUuidProvider instanceUuidProvider) {
        this.instanceUuidProvider = instanceUuidProvider;
    }
}
