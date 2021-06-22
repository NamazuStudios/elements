package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.RoutingStatus;
import org.zeromq.ZMsg;

import java.util.ArrayList;
import java.util.List;

import static com.namazustudios.socialengine.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;

public class JeroMQRoutingStatus implements RoutingStatus {

    private List<Route> routingTable;

    private final InstanceId instanceId;

    public JeroMQRoutingStatus(final ZMsg zMsg) {

        this.instanceId =new InstanceId(zMsg.removeFirst().getData());

        final var routingTable = new ArrayList<Route>();
        while (!zMsg.isEmpty()) routingTable.add(new JeroMQRoute(zMsg));
        this.routingTable = unmodifiableList(routingTable);

    }

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Override
    public List<Route> getRoutingTable() {
        return routingTable;
    }

    private class JeroMQRoute implements Route {

        private final boolean local;

        private final NodeId nodeId;

        private final String physicalDestination;

        public JeroMQRoute(final ZMsg zMsg) {
            nodeId = NodeId.nodeIdFromBytes(zMsg.removeFirst().getData());
            local = instanceId.equals(nodeId.getInstanceId());
            physicalDestination = zMsg.removeFirst().getString(CHARSET);
        }

        @Override
        public boolean isLocal() {
            return local;
        }

        @Override
        public NodeId getNodeId() {
            return nodeId;
        }

        @Override
        public String getPhysicalDestination() {
            return physicalDestination;
        }

        @Override
        public String toString() {
            return format("(%s%s %s) %s -> %s",
                isLocal() ? "L" : "R" ,
                nodeId.isMaster() ? "M" : "W" ,
                nodeId.getInstanceId(),
                nodeId,
                physicalDestination
            );
        }

    }

}
