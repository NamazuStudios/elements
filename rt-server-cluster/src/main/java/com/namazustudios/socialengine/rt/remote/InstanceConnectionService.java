package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.InstanceMetadataContext;
import com.namazustudios.socialengine.rt.Subscription;
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

    /**
     * Starts the service.
     */
    void start();

    /**
     * Stops the service.
     */
    void stop();

    /**
     * Forces a refresh from the latest source of data immediately.  This will ensure that new
     * {@link InstanceConnection}s are added and stale ones removed immediately.  All necessary events will be driven
     * as part of this call.
     */
    void refresh();

    /**
     * Gets the {@link InstanceId} for this {@link InstanceConnectionService}.
     *
     * @return the {@link InstanceId}
     */
    InstanceId getInstanceId();

    /**
     * Gets an {@link InstanceBinding} which a node can then use to receive incoming data.  If a binding already exists
     * for the supplied {@link NodeId}, then an exception will be thrown.
     *
     * @param nodeId the {@link NodeId} for which the binding will be used.
     * @return the {@link InstanceBinding}
     */
    InstanceBinding openBinding(NodeId nodeId);

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
     * Returns the local control address.  This is an address through which the a control client may be used to control
     * the instance from within the local process space.
     *
     * @return the local control address.
     */
    String getLocalControlAddress();

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
         * Opens a route to the supplied node with {@link NodeId}, returning an address where it will be possible to
         * connect using a {@link RemoteInvoker}.
         *
         * @param nodeId
         * @return
         */
        String openRouteToNode(NodeId nodeId);

        /**
         * Disconnects and disposes of the underlying {@link InstanceConnection}.  Trying to use the
         * {@link InstanceMetadataContext} belonging to this connection will fail immediately.
         */
        void disconnect();

    }

    /**
     * Represents a binding.  The binding is managed by the {@link InstanceConnectionService}.  Wen opening a binding,
     * a node will then bind on the provided address.  The {@link InstanceConnectionService} will accept connections
     * until the binding is closed.
     */
    interface InstanceBinding extends AutoCloseable {

        /**
         * Gets the {@link NodeId} this {@link InstanceBinding} represents.
         *
         * @return the {@link NodeId}
         */
        NodeId getNodeId();

        /**
         * Returns the host address to which to bind when receiving incoming data.
         *
         * @return the bind address
         */
        String getBindAddress();

        /**
         * Closes the binding.
         */
        @Override
        void close();

    }

}
