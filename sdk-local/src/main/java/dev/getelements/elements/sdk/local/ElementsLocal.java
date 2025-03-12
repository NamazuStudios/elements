package dev.getelements.elements.sdk.local;

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
     * Closes this {@link ElementsLocal}.
     */
    @Override
    void close();

}
