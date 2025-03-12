package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.ElementRegistry;

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
     * Gets the root {@link ElementRegistry} used by the local runner.
     *
     * @return the {@link ElementRegistry}
     */
    ElementRegistry getRootElementRegistry();

    /**
     * Closes this {@link ElementsLocal}.
     */
    @Override
    void close();

}
