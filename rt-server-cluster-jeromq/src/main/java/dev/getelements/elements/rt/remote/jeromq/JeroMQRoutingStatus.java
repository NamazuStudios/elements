package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.rt.id.InstanceId;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.remote.RoutingStatus;
import org.zeromq.ZMsg;

import java.util.ArrayList;
import java.util.List;

import static dev.getelements.elements.rt.remote.jeromq.JeroMQRoutingServer.CHARSET;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableList;

public class JeroMQRoutingStatus implements RoutingStatus {

    private final List<Route> routingTable;

    private final List<Route> masterRoutingTable;

    private final List<Route> applicationNodeRoutingTable;

    private final InstanceId instanceId;

    public JeroMQRoutingStatus(final ZMsg zMsg) {

        this.instanceId =new InstanceId(zMsg.removeFirst().getData());

        final var routingTable = new ArrayList<Route>();
        while (!zMsg.isEmpty()) routingTable.add(new JeroMQRoute(zMsg));
        this.routingTable = unmodifiableList(routingTable);

        this.masterRoutingTable = routingTable
            .stream()
            .filter(r -> r.getNodeId().isMaster())
            .collect(toUnmodifiableList());

        this.applicationNodeRoutingTable = routingTable
            .stream()
            .filter(r -> !r.getNodeId().isMaster())
            .collect(toUnmodifiableList());

    }

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Override
    public List<Route> getRoutingTable() {
        return routingTable;
    }

    @Override
    public List<Route> getMasterNodeRoutingTable() {
        return masterRoutingTable;
    }

    @Override
    public List<Route> getApplicationNodeRoutingTable() {
        return applicationNodeRoutingTable;
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
