package com.namazustudios.socialengine;

import com.namazustudios.socialengine.rt.transact.JournalTransactionalResourceServicePersistence;
import com.namazustudios.socialengine.rt.transact.unix.UnixFSJournalTransactionalPersistenceDriver;
import com.namazustudios.socialengine.rt.xodus.XodusTransactionalResourceServicePersistence;
import com.namazustudios.socialengine.test.JeroMQEmbeddedTestService;

import static com.google.inject.Guice.createInjector;

public class TestUtils {

    /**
     * Creates a test case with the {@link XodusTransactionalResourceServicePersistence}
     *
     * @param testClass the type to construct
     * @param <T> the type to return
     * @return the constructed test case
     */
    public static <T> T getXodusTest(Class<T> testClass) {
        final var module = new UnitTestModule(new JeroMQEmbeddedTestService().withXodusWorker());
        final var injector = createInjector(module);
        return injector.getInstance(testClass);
    }

    /**
     * Creates a test case with the {@link JournalTransactionalResourceServicePersistence} backed by the
     * {@link UnixFSJournalTransactionalPersistenceDriver}
     *
     * @param testClass the type to construct
     * @param <T> the type to return
     * @return the constructed test case
     */
    public static <T> T getUnixFSTest(Class<T> testClass) {
        final var module = new UnitTestModule(new JeroMQEmbeddedTestService().withUnixFSWorker());
        final var injector = createInjector(module);
        return injector.getInstance(testClass);
    }

}
