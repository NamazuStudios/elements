package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.cluster.id.InstanceId;
import dev.getelements.elements.sdk.cluster.id.NodeId;
import dev.getelements.elements.rt.remote.AsyncOperation;
import dev.getelements.elements.rt.remote.Node;
import dev.getelements.elements.rt.remote.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.Set;
import java.util.function.Consumer;

import static dev.getelements.elements.rt.remote.NodeState.HEALTHY;
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
        try (final var accessor = getWorker().accessWorkerState()) {
            return accessor.getNodeSet()
                .stream()
                .filter(node -> HEALTHY.equals(node.getState()))
                .map(Node::getNodeId)
                .collect(toSet());
        }
    }

    @Override
    public double getInstanceQuality() {
        return getLoadMonitorService().getInstanceQuality();
    }

    @Override
    public AsyncOperation getInstanceMetadataAsync(final Consumer<InstanceMetadata> success,
                                                   final Consumer<Throwable> failure) {

        try (var accessor = getWorker().accessWorkerState()) {

            final var quality = getLoadMonitorService().getInstanceQuality();
            final var nodeIdSet = accessor.getNodeSet()
                    .stream()
                    .filter(node -> HEALTHY.equals(node.getState()))
                    .map(Node::getNodeId)
                    .collect(toSet());

            logger.debug("Reporting instance quality {} - {}", instanceId, quality);
            logger.debug("Returning active node IDs for instance {} - {}", instanceId, nodeIdSet);

            final var metadata = new InstanceMetadata();
            metadata.setQuality(quality);
            metadata.setNodeIds(nodeIdSet);
            success.accept(metadata);

            return AsyncOperation.DEFAULT;

        } catch (Exception ex) {
            failure.accept(ex);
            logger.error("Caught exception getting instance metadata.", ex);
            return AsyncOperation.DEFAULT;
        }
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
