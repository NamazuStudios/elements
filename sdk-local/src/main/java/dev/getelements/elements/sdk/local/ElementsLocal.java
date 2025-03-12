package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.ServiceLocator;

/**
 * Runs a local instance of Elements suitable for debugging.
 */
public interface ElementsLocal extends AutoCloseable {

    /**
     * Starts this {@link ElementsLocal}.
     */
    void start();

    /**
     * Runs the instance and will block until shutdown.
     */
    void run();

    /**
     * Gets the {@link ServiceLocator} for the core system types.
     */
    ServiceLocator getServiceLocator();

    /**
     * Closes this {@link ElementsLocal}.
     */
    @Override
    void close();

}
