package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.rt.exception.InvalidNodeIdException;
import dev.getelements.elements.rt.id.NodeId;
import org.zeromq.ZMsg;

import static dev.getelements.elements.rt.id.NodeId.nodeIdFromBytes;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQControlResponseCode.NO_SUCH_NODE_ROUTE;

public class JeroMQUnroutableNodeException extends JeroMQControlException {

    private final NodeId nodeId;

    public JeroMQUnroutableNodeException(final JeroMQUnroutableNodeException cause) {
        super(cause);
        this.nodeId = cause.nodeId;
    }

    public JeroMQUnroutableNodeException(final NodeId nodeId) {
        super(NO_SUCH_NODE_ROUTE);
        this.nodeId = nodeId;
    }

    public JeroMQUnroutableNodeException(final ZMsg response) {
        super(NO_SUCH_NODE_ROUTE, response);
        nodeId = parseNodeId(response);
    }

    private static NodeId parseNodeId(final ZMsg response) {

        if (response.isEmpty()) return null;

        try {
            final byte[] data = response.removeFirst().getData();
            return nodeIdFromBytes(data);
        } catch (InvalidNodeIdException ex) {
            return null;
        }

    }

    /**
     * Gets the {@link NodeId} that was returned.
     *
     * @return the {@link NodeId}
     */
    public NodeId getNodeId() {
        return nodeId;
    }

}
