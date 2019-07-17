package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.List;

public interface NodeConnectionService {

    List<NodeConnection> getActiveConnections();

    interface NodeConnection {

        NodeId getNodeId();

        void disconnect();

    }

}
