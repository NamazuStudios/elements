package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceBinding;

import javax.inject.Named;

/**
 * Represents the worker node for a single Application.  This is a simple interface which in and of itself does nothing
 * more than provide a means to start and stop the service. Conceptually, however, a Node represents the core
 * functionality for a given Application, e.g. resource management, task management--generally everything related to
 * Application logic execution.
 *
 * Additionally the node itself is responsible for opening and maintaining the connection.  The logic housed in the
 * node will receive incoming messages and dispatch them to local services.
 */
public interface Node extends AutoCloseable {

    /**
     * An indicator of the Node's name to be used with the {@link Named} annotation.
     */
    String NODE_NAME = "com.namazustudios.socialengine.rt.node.name";

    /**
     * Designates a master/control {@link Node}.  Each {@link Instance} has a master {@link Node} that handles the basic
     * services required to operate the {@link Node}.  This is used in conjunction with the {@link Named} annotation and
     * not the {@link Node#getName()} method.
     */
    String MASTER_NODE_NAME = "com.namazustudios.socialengine.rt.node.master";

    /**
     * Gets the symbolic name of the node.  This can be assigned by the container or assigned in configuration.  It
     * should be human readable, and should be unique per Instance.  Used just for symbolic naming of the {@link Node}
     * and in logging.
     *
     * @return the name of the node
     */
    String getName();

    /**
     * Gets the {@link NodeId}
     *
     * @return the {@link NodeId}
     */
    NodeId getNodeId();

    /**
     * Starts the service. This sets up any network listeners and begins accepting threads.  Once the service is up and
     * running, the service can begin to accept connections from clients.
     *
     * @param binding an {@link InstanceBinding} representing an open connection used by this {@link Node} to communicate
     * @throws IllegalStateException if the node has already been started
     */
    void start(InstanceBinding binding);

    /**
     * Stops the service.  This gracefully shuts down any worker threads, closes sockets, and takes the node offline.
     * Once this method returns, the service is completely offline.
     *
     * @throws IllegalStateException if the node has already been stopped
     */
    void stop();

    /**
     * Equivalent to calling {@link #stop()}, but allows this {@link Node} to be used in the instance of an
     * try-with-resources block.
     */
    @Override
    default void close() {
        stop();
    }

}
