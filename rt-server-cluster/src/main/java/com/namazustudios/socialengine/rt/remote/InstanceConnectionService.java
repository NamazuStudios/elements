package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.InstanceMetadataContext;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.List;
import java.util.function.Consumer;

public interface InstanceConnectionService {

    String getControlAddress();

    /**
     * Starts the service.
     */
    void start();

    /**
     * Stops the service.
     */
    void stop();

    /**
     * Connects to an instance with the remote address.  Once connected, this will drive all applicable callbacks.
     *
     * @param remoteAddress the remote address, expressed as the underlying implementation's connection string format
     * @return
     */
    InstanceConnection connect(String remoteAddress);

    /**
     * Gets a {@link List<InstanceConnection>} representing all active connections.
     *
     * @return the list of all active {@link InstanceConnection}s
     */
    List<InstanceConnection> getActiveConnections();

    /**
     * Adds a {@link Consumer<InstanceConnection>} that will be called when a new instance has connected to this
     * {@link InstanceConnectionService}.
     *
     * @param onConnect the {@link Consumer<InstanceConnection>}
     * @return a {@link Subscription} that can be cancled later.
     */
    Subscription subscribeToConnect(Consumer<InstanceConnection> onConnect);

    /**
     * Adds a {@link Consumer<InstanceConnection>} that will be called when a an instance has disconnected from this
     * {@link InstanceConnectionService}.
     *
     * @param onDisconnect the {@link Consumer<InstanceConnection>}
     * @return a {@link Subscription} that can be cancled later.
     */
    Subscription subscribeToDisconnect(Consumer<InstanceConnection> onDisconnect);

    /**
     * Opens a {@lin}
     * @param nodeId
     * @return
     */
    String getRoute(NodeId nodeId);


    /**
     * Represents a connection to a remote instance.
     */
    interface InstanceConnection {

        /**
         * Gets the {@link InstanceId} represented by this {@link InstanceConnection}
         *
         * @return
         */
        InstanceId getInstanceId();

        /**
         * Get the {@link InstanceMetadataContext} assoicated with
         * @return
         */
        InstanceMetadataContext getInstanceMetadataContext();

        /**
         * Disconnects and disposes of the underlying {@link InstanceConnection}
         */
        void disconnect();

    }

    /**
     * Returned from the various subscribe calls.  Can be used to cancel the subscription.
     */
    @FunctionalInterface
    interface Subscription {

        /**
         * Unsubscribes from the
         */
        void unsubscribe();

    }

}
