package dev.getelements.elements.service;

import com.google.inject.Injector;
import dev.getelements.elements.rt.transact.JournalTransactionalResourceServicePersistenceEnvironment;
import dev.getelements.elements.rt.transact.unix.UnixFSJournalTransactionalPersistenceDriver;
import dev.getelements.elements.rt.xodus.XodusTransactionalResourceServicePersistenceEnvironment;

import static com.google.inject.Guice.createInjector;

public class TestUtils {

    private static final TestUtils instance = new TestUtils();

    public static TestUtils getInstance() {
        return instance;
    }

    private final Injector xodusInjector;

    private final Injector unixFSInjector;

    private TestUtils() {

        final var xodusModule = new XodusIntegrationTestModule();
        xodusInjector = createInjector(xodusModule);

        final var unixFSModule = new UnixFSIntegrationTestModule();
        unixFSInjector = createInjector(unixFSModule);

    }

    /**
     * Creates a test case with the {@link XodusTransactionalResourceServicePersistenceEnvironment}
     *
     * @param testClass the type to construct
     * @param <T> the type to return
     * @return the constructed test case
     */
    public <T> T getXodusTest(final Class<T> testClass) {
        return xodusInjector.getInstance(testClass);
    }

    /**
     * Creates a test case with the {@link JournalTransactionalResourceServicePersistenceEnvironment} backed by the
     * {@link UnixFSJournalTransactionalPersistenceDriver}
     *
     * @param testClass the type to construct
     * @param <T> the type to return
     * @return the constructed test case
     */
    public <T> T getUnixFSTest(final Class<T> testClass) {
        return unixFSInjector.getInstance(testClass);
    }

}
