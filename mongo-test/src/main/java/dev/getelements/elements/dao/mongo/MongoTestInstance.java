package dev.getelements.elements.dao.mongo;

public interface MongoTestInstance extends AutoCloseable {

    void start();

    @Override
    void close();

    default void stop() {
        close();
    }

}

