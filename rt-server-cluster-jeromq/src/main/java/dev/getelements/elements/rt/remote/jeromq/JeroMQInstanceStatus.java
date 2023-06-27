package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.rt.id.InstanceId;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.remote.InstanceStatus;
import org.zeromq.ZFrame;
import org.zeromq.ZMsg;

import java.util.ArrayList;
import java.util.List;

import static dev.getelements.elements.rt.id.NodeId.nodeIdFromBytes;
import static dev.getelements.elements.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static java.util.Collections.unmodifiableList;

public class JeroMQInstanceStatus implements InstanceStatus {

    private final InstanceId instanceId;

    private final List<NodeId> nodeIds;

    public JeroMQInstanceStatus(final ZMsg zMsg) {

        if (zMsg.size() < 1) throw new IllegalArgumentException("Invalid JeroMQInstanceStatus");

        final byte[] instanceIdBytes = zMsg.removeFirst().getData();
        this.instanceId = new InstanceId(instanceIdBytes);

        final List<NodeId> nodeIds = new ArrayList<>();

        while (!zMsg.isEmpty()) {
            final ZFrame frame = zMsg.removeFirst();
            final NodeId nodeId = nodeIdFromBytes(frame.getData());
            nodeIds.add(nodeId);
        }

        this.nodeIds = unmodifiableList(nodeIds);

    }

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Override
    public List<NodeId> getNodeIds() {
        return nodeIds;
    }

    @Override
    public String toString() {
        return "JeroMQInstanceStatus{" +
                "instanceId=" + instanceId +
                ", nodeIds=" + nodeIds +
                '}';
    }

}
