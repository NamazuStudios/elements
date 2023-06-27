package dev.getelements.elements.rt;

/**
 * Drives the instance-wide persistence system, if available. Currently this is just allows for the start and stop
 * operations.
 */
public interface PersistenceEnvironment {

    /**
     * Starts the {@link PersistenceEnvironment} instance and obtains all resources necessary to begin accessing the
     * underlying data store.
     */
    void start();

    /**
     * Closes this {@link PersistenceEnvironment} instance and releases any underlying connections to
     * the data storage. Outstanding transactions may be forcibly closed if this is called.
     */
    void stop();

}
