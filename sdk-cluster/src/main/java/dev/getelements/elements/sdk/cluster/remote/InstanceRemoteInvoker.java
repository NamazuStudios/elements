package dev.getelements.elements.sdk.cluster.remote;

import dev.getelements.elements.sdk.Subscription;
import dev.getelements.elements.sdk.cluster.remote.dto.InstanceMetadata;

import java.util.function.Consumer;

/**
 * An extension of {@link RemoteInvoker} which is tied to a specific instance. This remote invoker carries with it
 */
public interface InstanceRemoteInvoker extends RemoteInvoker {

    /**
     * Gets the last known {@link InstanceMetadata}.
     *
     * @return the instance metadata
     */
    InstanceMetadata getInstanceMetadata();

    /**
     * Called when the {@link InstanceRemoteInvoker} receives an update from the remote.
     *
     * @param onMetadata called when new metadata is availble
     * @return a subscription
     */
    Subscription onMetadataUpdate(Consumer<InstanceMetadata> onMetadata);

}
