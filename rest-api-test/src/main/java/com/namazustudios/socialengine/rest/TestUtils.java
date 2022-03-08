package com.namazustudios.socialengine.rest;

import com.google.inject.Injector;
import com.namazustudios.socialengine.rt.transact.JournalTransactionalResourceServicePersistenceEnvironment;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSJournalTransactionalPersistenceDriver;
import com.namazustudios.socialengine.rt.xodus.XodusTransactionalResourceServicePersistenceEnvironment;

import static com.google.inject.Guice.createInjector;

public class TestUtils {

    public static final String TEST_API_ROOT = "com.namazustudios.socialengine.rest.test.api.root";

    private static final TestUtils instance = new TestUtils();

    public static TestUtils getInstance() {
        return instance;
    }

    private final Injector xodusInjector;

    private final Injector unixFSInjector;

    private TestUtils() {

        final var xodusModule = new XodusEmbeddedRestApiIntegrationTestModule();
        xodusInjector = createInjector(xodusModule);

        final var unixFSModule = new UnixFSEmbeddedRestApiIntegrationTestModule();
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
