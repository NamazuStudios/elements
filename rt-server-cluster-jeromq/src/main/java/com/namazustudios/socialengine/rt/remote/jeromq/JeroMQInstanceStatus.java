package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.zeromq.ZFrame;
import org.zeromq.ZMsg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static java.util.Collections.unmodifiableList;

public class JeroMQInstanceStatus {

    private final InstanceId instanceId;

    private final List<NodeId> nodeIds;

    public JeroMQInstanceStatus(final ZMsg zMsg) {

        if (zMsg.size() != 1) throw new IllegalArgumentException("Invalid JeroMQInstanceStatus");

        final String instanceId = zMsg.removeFirst().getString(CHARSET);
        this.instanceId = new InstanceId(instanceId);

        final List<NodeId> nodeIds = new ArrayList<>();

        while (!zMsg.isEmpty()) {
            final ZFrame frame = zMsg.removeFirst();
            final NodeId nodeId = new NodeId(frame.getString(CHARSET));
            nodeIds.add(nodeId);
        }

        this.nodeIds = unmodifiableList(nodeIds);

    }

    public InstanceId getInstanceId() {
        return instanceId;
    }

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
