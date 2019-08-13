package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.InstanceDiscoveryService;
import com.namazustudios.socialengine.rt.exception.MultiException;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.namazustudios.socialengine.rt.remote.Node.MASTER_NODE_NAME;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

/**
 * Contains all {@link Node} instances for several {@link Node} instances and manages their life cycles therein.  This
 * imposes the additional requirement of providing some form of {@link InstanceConnectionService} to route internal
 * requests.
 */
public class WorkerInstance implements Instance {

    private static final Logger logger = LoggerFactory.getLogger(WorkerInstance.class);

    private InstanceId instanceId;

    private Node masterNode;

    private Set<Node> nodeSet;

    private InstanceDiscoveryService instanceDiscoveryService;

    private InstanceConnectionService instanceConnectionService;

    private final Set<InstanceBinding> bindingSet = new HashSet<>();

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Override
    public void start() {

        final List<Exception> exceptionList = new ArrayList<>();

        try {
            getInstanceDiscoveryService().start();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception starting InstanceDiscoveryService.", ex);
        }

        try {
            getInstanceConnectionService().start();
        } catch (Exception ex) {
            exceptionList.add(ex);
            logger.error("Caught exception starting InstanceConnectionService.", ex);
        }

        exceptionList.addAll(startAllNodes());

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }

    }

    private List<Exception> startAllNodes() {
        return concat(of(getMasterNode()), getNodeSet().stream()).map(node -> {

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

        }).filter(ex -> ex != null).collect(toList());
    }

    @Override
    public void close() {

        final List<Exception> exceptionList = new ArrayList<>();

        exceptionList.addAll(concat(of(getMasterNode()), getNodeSet().stream()).map(node -> {
            try {
                node.stop();
                return null;
            } catch (Exception ex) {
                logger.error("Error stopping node.", ex);
                return ex;
            }
        }).filter(ex -> ex != null).collect(toList()));

        exceptionList.addAll(bindingSet.stream().map(binding -> {
            try {
                binding.close();
                return null;
            } catch (Exception ex) {
                logger.error("Error stopping node.", ex);
                return ex;
            }
        }).filter(ex -> ex != null).collect(toList()));

        try {
            getInstanceConnectionService().stop();
        } catch (Exception ex) {
            exceptionList.add(ex);
        }

        try {
            getInstanceDiscoveryService().stop();
        } catch (Exception ex) {
            exceptionList.add(ex);
        }

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }

    }

    public Set<Node> getNodeSet() {
        return nodeSet;
    }

    @Inject
    public void setNodeSet(Set<Node> nodeSet) {
        this.nodeSet = nodeSet;
    }

    public InstanceDiscoveryService getInstanceDiscoveryService() {
        return instanceDiscoveryService;
    }

    @Inject
    public void setInstanceDiscoveryService(InstanceDiscoveryService instanceDiscoveryService) {
        this.instanceDiscoveryService = instanceDiscoveryService;
    }

    public InstanceConnectionService getInstanceConnectionService() {
        return instanceConnectionService;
    }

    @Inject
    public void setInstanceConnectionService(InstanceConnectionService instanceConnectionService) {
        this.instanceConnectionService = instanceConnectionService;
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
