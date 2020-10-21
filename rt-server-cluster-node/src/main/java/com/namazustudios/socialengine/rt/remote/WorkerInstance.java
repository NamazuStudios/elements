package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.remote.Node.MASTER_NODE_NAME;
import static java.util.stream.Collectors.toList;
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

        List<Node.Startup> startupList;

        startupList = concat(of(getMasterNode()), getNodeSet().stream()).map(node -> {
            try {
                logger.debug("Beginning node startup for node {}", node.getNodeId());
                return node.beginStartup();
            } catch (Exception ex) {
                logger.error("Error beginning node startup process.", ex);
                exceptionConsumer.accept(ex);
                return null;
            }
        }).filter(s -> s != null).collect(toList());

        startupList = startupList.stream().map(s -> {
            try {
                logger.debug("Executing pre-start operations for node {}", s.getNodeId());
                s.preStart();
                return s;
            } catch (Exception ex) {
                logger.error("Error in node pre-startup process.", ex);
                exceptionConsumer.accept(ex);
                s.cancel();
                return null;
            }
        }).filter(s -> s != null).collect(toList());

        startupList = startupList.stream().map(s -> {

            logger.debug("Opening binding for node id {}", s.getNodeId());

            final InstanceBinding binding = getInstanceConnectionService().openBinding(s.getNodeId());
            bindingSet.add(binding);

            logger.debug("Opened binding for node {}.", s.getNodeId());

            try {
                logger.debug("Executing start operations for node {}", s.getNodeId());
                s.start(binding);
                return s;
            } catch (Exception ex) {
                logger.error("Error in node startup process.", ex);
                exceptionConsumer.accept(ex);
                s.cancel();
                binding.close();
                return null;
            }
        }).filter(s -> s != null).collect(toList());

        // Performs a refresh of all connections to ensure that the underlying registries are all ready to go
        // by the time the rest of the system is started up.
        refreshConnections();

        startupList.stream().map(s -> {
            try {
                logger.debug("Executing post-start operations for node {}.", s.getNodeId());
                s.postStart();
                return null;
            } catch (Exception ex) {
                logger.error("Error in node post-startup process.", ex);
                exceptionConsumer.accept(ex);
                s.cancel();
                return ex;
            }
        }).filter(ex -> ex != null).forEach(exceptionConsumer);

    }

    @Override
    protected void preClose(final Consumer<Exception> exceptionConsumer) {

        final List<Node.Shutdown> shutdownList;

        shutdownList = concat(of(getMasterNode()), getNodeSet().stream()).map(node -> {
            try {
                return node.beginShutdown();
            } catch (Exception ex) {
                logger.error("Error beginning node shutdown process.", ex);
                exceptionConsumer.accept(ex);
                return null;
            }
        }).filter(s -> s != null).collect(toList());

        shutdownList.forEach(s -> {
            try {
                s.preStop();
            } catch (Exception ex) {
                logger.error("Error issuing node pre-shutdown process.", ex);
                exceptionConsumer.accept(ex);
            }
        });

        shutdownList.forEach(s -> {
            try {
                s.stop();
            } catch (Exception ex) {
                logger.error("Error issuing node shutdown process.", ex);
                exceptionConsumer.accept(ex);
            }
        });

        shutdownList.forEach(s -> {
            try {
                s.postStop();
            } catch (Exception ex) {
                logger.error("Error issuing node post-shutdown process.", ex);
                exceptionConsumer.accept(ex);
            }
        });

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
