package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.*;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import com.namazustudios.socialengine.rt.remote.AsyncOperation;

import java.util.Set;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.annotation.RemoteServiceDefinition.*;

/**
 * Provides data for an Instance, which is representative of the physical machine running one or more nodes.  Each
 * Node will have a special {@link NodeId} that allows the remote services to access the information about the
 * underlying instance.
 */
@Proxyable
@RemoteService(@RemoteServiceDefinition(scope = MASTER_SCOPE, protocol = ELEMENTS_RT_PROTOCOL))
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
     * Gets all {@link NodeId}s housed within the instance.
     *
     * @return the {@link Set<NodeId>} of all running and active nodes on the instance
     */
    @RemotelyInvokable
    Set<NodeId> getNodeIds();

    /**
     * Represents the instance's current load factor.  This returns a double in the range of [0, 1], with the higher
     * number representing a greater load.  Load represents a single average measurement of how loaded the system is
     * and this number may not necessary equate to CPU load.
     *
     * @return the instance's load
     */
    @RemotelyInvokable
    double getInstanceQuality();

    /**
     * Asynchronous method to fetch the
     *
     * @param success
     * @param failure
     * @return
     */
    @RemotelyInvokable
    AsyncOperation getInstanceMetadataAsync(@ResultHandler final Consumer<InstanceMetadata> success,
                                            @ErrorHandler final Consumer<Throwable> failure);

}
