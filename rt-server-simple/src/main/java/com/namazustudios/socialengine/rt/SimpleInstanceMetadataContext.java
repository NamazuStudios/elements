package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.Node;
import com.namazustudios.socialengine.rt.remote.Worker;

import javax.inject.Inject;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Provides data for an Instance.
 */
public class SimpleInstanceMetadataContext implements InstanceMetadataContext {

    private Worker worker;

    private InstanceId instanceId;

    private LoadMonitorService loadMonitorService;

    @Override
    public void start() {
        getLoadMonitorService().start();
    }

    @Override
    public void stop() {
        getLoadMonitorService().stop();
    }

    @Override
    public Set<NodeId> getNodeIds() {
        return getWorker().getActiveNodeIds();
    }

    @Override
    public double getInstanceLoad() {
        return getLoadMonitorService().getInstanceQuality();
    }

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    public Worker getWorker() {
        return worker;
    }

    @Inject
    public void setWorker(Worker worker) {
        this.worker = worker;
    }

    @Inject
    public void setInstanceId(InstanceId instanceId) {
        this.instanceId = instanceId;
    }

    public LoadMonitorService getLoadMonitorService() {
        return loadMonitorService;
    }

    @Inject
    public void setLoadMonitorService(LoadMonitorService loadMonitorService) {
        this.loadMonitorService = loadMonitorService;
    }

}
