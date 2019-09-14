package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.exception.InvalidNodeIdException;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.zeromq.ZMsg;

import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQControlResponseCode.NO_SUCH_NODE_ROUTE;

public class JeroMQUnroutableNodeException extends JeroMQControlException {

    private final NodeId nodeId;

    public JeroMQUnroutableNodeException(final NodeId nodeId) {
        super(NO_SUCH_NODE_ROUTE);
        this.nodeId = nodeId;
    }

    public JeroMQUnroutableNodeException(final ZMsg response) {
        super(NO_SUCH_NODE_ROUTE);
        nodeId = parseNodeId(response);
    }

    private static NodeId parseNodeId(final ZMsg response) {

        if (response.isEmpty()) return null;

        try {
            final byte[] data = response.removeFirst().getData();
            return new NodeId(data);
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
