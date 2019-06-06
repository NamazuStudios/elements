package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.MultiException;
import com.namazustudios.socialengine.rt.remote.ConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.namazustudios.socialengine.rt.Node.MASTER_NODE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.UUID.nameUUIDFromBytes;
import static java.util.stream.Collectors.toList;

/**
 * Contains all {@link Node} instances for several {@link Node} instances and manages their life cycles therein.  This
 * imposes the additional requirement of providing some form of {@link ConnectionService} to route internal
 * requests.
 */
public class MultiNodeContainer implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MultiNodeContainer.class);

    private Set<Node> nodeSet;

    private Node masterNode;

    private ConnectionService connectionService;

    /**
     * Attempts to start each {@link Node}, throwing an instance of {@link MultiException} if any {@link Node} fails
     * to startup.
     */
    public void start() {

        final List<Exception> exceptionList = new ArrayList<>();

        try {
            getConnectionService().start();
        } catch (Exception ex) {
            exceptionList.add(ex);
        }

        getNodeSet().forEach(node -> {
            final UUID inprocIdentifier = node.getNodeId().getApplicationUuid();

            getConnectionService().issueConnectInprocCommand(null, inprocIdentifier);
        });

        exceptionList.addAll(getNodeSet().parallelStream().map(node -> {
            try {
                node.start();
                return null;
            } catch (Exception ex) {
                logger.error("Error starting node.", ex);
                return ex;
            }
        }).filter(ex -> ex != null).collect(toList()));

        try {
            getMasterNode().start();

            // for now, by convention we use the instance uuid for the inproc address for the master node
            final UUID inprocIdentifier = getMasterNode().getNodeId().getInstanceUuid();

            getConnectionService().issueConnectInprocCommand(null, inprocIdentifier);
        }
        catch (Exception e) {
            logger.error("Error stopping node.", e);
            exceptionList.add(e);
        }

        if (!exceptionList.isEmpty()) {
            throw new MultiException(exceptionList);
        }

    }

    /**
     * Attempts to stop each {@link Node}, throwing an instance of {@link MultiException} if
     */
    @Override
    public void close() {

        final List<Exception> exceptionList = getNodeSet().parallelStream().map(node -> {
            try {
                node.stop();
                return null;
            } catch (Exception ex) {
                logger.error("Error stopping node.", ex);
                return ex;
            }
        }).filter(ex -> ex != null).collect(toList());

        try {
            getMasterNode().stop();
        }
        catch (Exception e) {
            logger.error("Error stopping master node.", e);
            exceptionList.add(e);
        }

        try {
            getConnectionService().stop();
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

    public ConnectionService getConnectionService() {
        return connectionService;
    }

    @Inject
    public void setConnectionService(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    public Node getMasterNode() {
        return masterNode;
    }

    @Inject
    public void setMasterNode(@Named(MASTER_NODE) Node masterNode) {
        this.masterNode = masterNode;
    }
}
