package dev.getelements.elements.rt.transact;

/**
 * An opaque context which is started first and stopped last by the {@link TransactionalResourceServicePersistence}.
 * This is responsible for handling any internal configuration and locking of the underlying datastore such that only
 * one JVM process may have access to it at any given time.
 */
public interface JournalTransactionalPersistenceDriver {

    /**
     * Starts the {@link JournalTransactionalPersistenceDriver}.
     */
    void start();

    /**
     * Stops the {@link JournalTransactionalPersistenceDriver}.
     */
    void stop();

}
