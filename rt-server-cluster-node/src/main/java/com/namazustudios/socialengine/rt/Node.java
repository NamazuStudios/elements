package com.namazustudios.socialengine.rt;

/**
 * Represents the worker node.  This is a simple interface which does nothing more than simply provide a means to start
 * and stop the service.
 */
public interface Node extends AutoCloseable {

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
