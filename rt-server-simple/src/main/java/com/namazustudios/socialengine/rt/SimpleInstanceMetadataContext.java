package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.Node;
import com.namazustudios.socialengine.rt.remote.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Provides data for an Instance.
 */
public class SimpleInstanceMetadataContext implements InstanceMetadataContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleInstanceMetadataContext.class);

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
        final Set<NodeId> nodeIdSet = getWorker().getActiveNodeIds();
        logger.debug("Returning active node IDs for instance {} - {}", instanceId, nodeIdSet);
        return nodeIdSet;
    }

    @Override
    public double getInstanceQuality() {
        final double load = getLoadMonitorService().getInstanceQuality();
        logger.debug("Reporting instance load {} - {}", instanceId, load);
        return load;
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
