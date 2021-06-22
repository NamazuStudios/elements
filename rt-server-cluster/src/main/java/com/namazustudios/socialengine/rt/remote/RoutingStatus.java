package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.List;

public interface RoutingStatus {

    InstanceId getInstanceId();

    List<Route> getRoutingTable();

    interface Route {

        boolean isLocal();

        NodeId getNodeId();

        String getPhysicalDestination();

    }

}
