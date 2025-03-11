package dev.getelements.elements.dao.mongo.test;

/**
 * A test instance runner for mongo.
 */
public interface MongoTestInstance extends AutoCloseable {

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

