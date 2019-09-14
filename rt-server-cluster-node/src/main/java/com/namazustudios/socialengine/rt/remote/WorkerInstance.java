package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.remote.Node.MASTER_NODE_NAME;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

/**
 * Contains all {@link Node} instances for several {@link Node} instances and manages their life cycles therein.  This
 * imposes the additional requirement of providing some form of {@link InstanceConnectionService} to route internal
 * requests.
 */
public class WorkerInstance extends SimpleInstance implements Worker {

    private static final Logger logger = LoggerFactory.getLogger(WorkerInstance.class);

    private Node masterNode;

    private Set<Node> nodeSet;

    private final Set<InstanceBinding> bindingSet = new HashSet<>();

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Override
    protected void postStart(final Consumer<Exception> exceptionConsumer) {
        concat(of(getMasterNode()), getNodeSet().stream()).map(node -> {

            final InstanceBinding binding = getInstanceConnectionService().openBinding(node.getNodeId());
            bindingSet.add(binding);

            try {
                node.start(binding);
                return null;
            } catch (Exception ex) {
                logger.error("Error starting node closing {}", ex, binding);
                binding.close();
                return ex;
            }

        }).filter(ex -> ex != null).forEach(exceptionConsumer::accept);
    }

    @Override
    protected void preClose(final Consumer<Exception> exceptionConsumer) {

        concat(Stream.of(getMasterNode()), getNodeSet().stream()).map(node -> {
            try {
                node.stop();
                return null;
            } catch (Exception ex) {
                logger.error("Error stopping node {}.", node, ex);
                return ex;
            }
        }).filter(ex -> ex != null).forEach(exceptionConsumer::accept);

        bindingSet.stream().map(binding -> {
            try {
                binding.close();
                return null;
            } catch (Exception ex) {
                WorkerInstance.logger.error("Error closing binding {}.", binding, ex);
                return ex;
            }
        }).filter(ex -> ex != null).forEach(exceptionConsumer::accept);


    }

    @Override
    public Set<NodeId> getActiveNodeIds() {
        return getNodeSet().stream().map(n -> n.getNodeId()).collect(toSet());
    }

    public Set<Node> getNodeSet() {
        return nodeSet;
    }

    @Inject
    public void setNodeSet(Set<Node> nodeSet) {
        this.nodeSet = nodeSet;
    }

    public Node getMasterNode() {
        return masterNode;
    }

    @Inject
    public void setMasterNode(@Named(MASTER_NODE_NAME) Node masterNode) {
        this.masterNode = masterNode;
    }

    @Inject
    public void setInstanceId(InstanceId instanceId) {
        this.instanceId = instanceId;
    }

}
