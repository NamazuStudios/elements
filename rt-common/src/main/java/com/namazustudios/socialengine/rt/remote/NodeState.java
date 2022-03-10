package com.namazustudios.socialengine.rt.remote;

/**
 * Defines the state of the Node.
 */
public enum NodeState {

    /**
     * The node is ready to start.
     */
    READY,

    /**
     * The node is stopped.
     */
    STOPPED,

    /**
     * The node is in the process of starting.
     */
    STARTING,

    /**
     * The node has been started, but has not yet started to take requests.
     */
    STARTED,

    /**
     * The node is ready to receive requests.
     */
    HEALTHY,

    /**
     * The node is in the process of stoppping.
     */
    STOPPING,

    /**
     * The node is unhealthy and will need attention.
     */
    UNHEALTHY

}
