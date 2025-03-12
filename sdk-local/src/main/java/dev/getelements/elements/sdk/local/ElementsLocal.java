package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.ServiceLocator;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;

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
     * Gets the root {@link ElementRegistry} used by the local runner.
     *
     * @return the {@link ElementRegistry}
     */
    default ElementRegistry getRootElementRegistry() {
        return getServiceLocator().getInstance(ElementRegistry.class, ROOT);
    }

    /**
     * Closes this {@link ElementsLocal}.
     */
    @Override
    void close();

}
