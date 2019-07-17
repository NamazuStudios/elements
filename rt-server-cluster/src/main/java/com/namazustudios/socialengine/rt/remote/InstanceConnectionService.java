package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.InstanceMetadataContext;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;

import java.util.List;
import java.util.function.Consumer;

/**
 * The instance connection service handles the dirty details of connecting to a remote Node and keeping track of
 * nodes connected in the system.  It is essentially the communication nexus for the rest of the system and provides
 * client code the ability to connect and get basic information on remote nodes.
 */
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
     * Opens a route to the supplied node with {@link NodeId}, returning an address where it will be possible to connect
     * using a {@link RemoteInvoker}.
     *
     * @param nodeId
     * @return
     */
    String getRoute(NodeId nodeId);

    /**
     * Represents a connection to a remote instance.
     */
    interface InstanceConnection {

        /**
         * Gets the {@link InstanceId} represented by this {@link InstanceConnection}.  The value here is stored locally
         * and does not require a connection to the network.  Therefore it is still safe to call this method after the
         * remote end disconnects.
         *
         * @return the {@link InstanceId} for the connection
         */
        InstanceId getInstanceId();

        /**
         * Get the {@link InstanceMetadataContext} associated with the remote instance.  This will be a remote proxy for
         * the instance and will not be available after a call to {@link #disconnect()} is made.
         *
         * @return the {@link InstanceMetadataContext}
         */
        InstanceMetadataContext getInstanceMetadataContext();

        /**
         * Disconnects and disposes of the underlying {@link InstanceConnection}.  Trying to use the
         * {@link InstanceMetadataContext} belonging to this connection will fail immediately.
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
