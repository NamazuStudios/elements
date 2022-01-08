package com.namazustudios.socialengine.rt;

/**
 * Manages the environment for the scheduler subsystem.
 */
public interface SchedulerEnvironment {

    /**
     * Starts the {@link SchedulerEnvironment} instance and obtains all resources necessary to begin accessing the
     * underlying data store.
     */
    void start();

    /**
     * Closes this {@link SchedulerEnvironment} instance and releases any underlying connections to
     * the data storage. Outstanding transactions may be forcibly closed if this is called.
     */
    void stop();

}
