package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.Proxyable;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.Set;
import java.util.UUID;

/**
 * Provides data for an Instance, which is representative of the physical machine running one or more nodes.  Each
 * Node will have a special {@link NodeId} that allows the remote services to access the information about the
 * underlying instance.
 */
@Proxyable
public interface InstanceMetadataContext {

    /**
     * Starts this {@link InstanceMetadataContext}.
     */
    default void start() {}

    /**
     * Stops this {@link InstanceMetadataContext}.
     */
    default void stop() {}

    /**
     * Returns the {@link NodeId} for the instance context.  As the instance metadata context runs in a node, the
     * instance will have a {@link NodeId} where the instance and the node share the same ID.
     *
     * @return the {@link NodeId}
     */
    @RemotelyInvokable
    InstanceId getInstanceId();

    @RemotelyInvokable
    Set<NodeId> getNodeIds();

    @RemotelyInvokable
    double getInstanceQuality();

}
