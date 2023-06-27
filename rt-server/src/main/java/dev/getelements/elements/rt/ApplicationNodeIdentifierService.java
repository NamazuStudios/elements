package dev.getelements.elements.rt;

import java.util.UUID;


public interface ApplicationNodeIdentifierService extends AutoCloseable {

    default void start() {}

    /**
     *
     * @throws IllegalStateException
     * @return
     */
    UUID getOrCreateApplicationNodeIdentifier() throws IllegalStateException;

    @Override
    void close();

}
