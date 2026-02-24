package dev.getelements.elements.rest.test;

import com.google.inject.Injector;
import dev.getelements.elements.rt.transact.JournalTransactionalResourceServicePersistenceEnvironment;
import dev.getelements.elements.rt.transact.unix.UnixFSJournalTransactionalPersistenceDriver;

import static com.google.inject.Guice.createInjector;

public class TestUtils {

    public static final String TEST_INSTANCE = "dev.getelements.elements.test.instance";

    public static final String TEST_API_ROOT = "dev.getelements.elements.rest.test.api.root";

    public static final String TEST_APP_SERVE_RS_ROOT = "dev.getelements.elements.rest.test.appserve.rs.root";

    public static final String TEST_APP_SERVE_WS_ROOT = "dev.getelements.elements.rest.test.appserve.ws.root";

    private static final TestUtils instance = new TestUtils();

    public static TestUtils getInstance() {
        return instance;
    }

    private final Injector unixFSInjector;

    private TestUtils() {
        final var module = new EmbeddedRestApiIntegrationTestModule();
        unixFSInjector = createInjector(module);
    }

    /**
     * Creates a test case with the {@link JournalTransactionalResourceServicePersistenceEnvironment} backed by the
     * {@link UnixFSJournalTransactionalPersistenceDriver}
     *
     * @param testClass the type to construct
     * @param <T> the type to return
     * @return the constructed test case
     */
    public <T> T getTestFixture(final Class<T> testClass) {
        return unixFSInjector.getInstance(testClass);
    }

}
