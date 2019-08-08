package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.List;

public interface InstanceStatus {
    InstanceId getInstanceId();

    List<NodeId> getNodeIds();
}
