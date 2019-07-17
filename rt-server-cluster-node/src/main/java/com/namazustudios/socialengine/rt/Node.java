package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.NodeId;

/**
 * Represents the worker node for a single Application.  This is a simple interface which in and of itself does nothing
 * more than provide a means to start and stop the service. Conceptually, however, a Node represents the core
 * functionality for a given Application, e.g. resource management, task management--generally everything related to
 * Application logic execution.
 */
public interface Node extends AutoCloseable {

    /**
     * Gets the {@link NodeId}
     *
     * @return
     */
    NodeId getNodeId();

    /**
     * Gets the name of the node.  This can be assigned by the container or assigned in configuration.  Must be human
     * readable, and should be unique per Instance.
     *
     * @return the name of the node
     */
    String getName();

    /**
     * Starts the service. This sets up any network listeners and begins accepting threads.  Once the service is up and
     * running, the service can begin to accept connections from clients.
     *
     * @throws IllegalStateException if the node has already been started
     */
    void start();

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
