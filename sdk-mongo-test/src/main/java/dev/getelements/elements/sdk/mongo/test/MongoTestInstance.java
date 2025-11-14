package dev.getelements.elements.sdk.mongo.test;

/**
 * A test instance runner for mongo.
 */
public interface MongoTestInstance extends AutoCloseable {

    /**
     * The tested version of Elements.
     */
    String ELEMENTS_TESTED_VERSION = "6.0.9";

    /**
     * Starts the test instance.
     */
    void start();

    /**
     * Closes and stops this mongo instance
     */
    @Override
    void close();

    /**
     * Stops which, by default, calls close.
     */
    default void stop() {
        close();
    }

}

